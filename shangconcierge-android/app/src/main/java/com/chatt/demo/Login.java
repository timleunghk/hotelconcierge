package com.chatt.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.utils.Utils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


public class Login extends AccountAuthenticatorActivity //CustomActivity
{

	/** The username edittext. */
	private EditText user;

	/** The password edittext. */
	private EditText pwd;


	public static final String ARG_ACCOUNT_TYPE = "accountType";
	public static final String ARG_AUTH_TOKEN_TYPE = "authTokenType";
	public static final String ARG_IS_ADDING_NEW_ACCOUNT = "isAddingNewAccount";
	public static final String PARAM_USER_PASSWORD = "password";

	private static final String TAG = "LoginActivity";

	private AccountManager mAccountManager;

	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUser;
	private String mPwd;

	/* (non-Javadoc)
	 * @see com.chatt.custom.CustomActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		mAccountManager = AccountManager.get(this);

		mUser = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
		user = (EditText) findViewById(R.id.user);
		user.setText(mUser);

		mPwd = getIntent().getStringExtra(PARAM_USER_PASSWORD);
		pwd = (EditText) findViewById(R.id.pwd);
		pwd.setText(mPwd);

		findViewById(R.id.btnLogin).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}


	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		user.setError(null);
		pwd.setError(null);

		// Store values at the time of the login attempt.
		mUser = user.getText().toString();
		mPwd = pwd.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mUser)) {
			user.setError(getString(R.string.err_fields_empty));
			focusView = user;
			cancel = true;
		}
		// Check for a valid email address.
		if (TextUtils.isEmpty(mPwd)) {
			pwd.setError(getString(R.string.err_fields_empty));
			focusView = pwd;
			cancel = true;
		}


		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		}
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			//mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			//showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	public class UserLoginTask extends AsyncTask<Void, Void, Intent> {

		@Override
		protected Intent doInBackground(Void... params) {

			// TODO: attempt authentication against a network service.
			String authToken = AccountUtils.mServerAuthenticator.signIn(mUser, mPwd);

			final Intent res = new Intent();
			res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUser);
			res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountUtils.ACCOUNT_TYPE);
			res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
			res.putExtra(PARAM_USER_PASSWORD, mPwd);

			return res;
		}

		@Override
		protected void onPostExecute(final Intent intent) {

			String mToken;

			mAuthTask = null;
			//showProgress(false);





			mToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
			Log.d(TAG, "Token from account manager:" + intent.getStringExtra(AccountManager.KEY_AUTHTOKEN));

			if (mToken==null)
			{
				Log.d(TAG, "Token from account manager is null");

				pwd.setError(getString(R.string.error_incorrect_password));
				pwd.requestFocus();
			}
			else
			{
				ParseUser.logOut(); //prevent
				Log.d(TAG, "chk user:"+ mUser + ",mPwd:"+mPwd);
				if (ChkUser(mUser) > 0) {
					Log.d(TAG, "mUser Found, do finish login");
					finishLogin(intent);
					Log.d(TAG, "mUser Found, finish login end");
					joinRoom(mUser, mPwd);
				}
				else
				{
					Log.d(TAG, "User cannot found, cannot login, do reg now");
					DoRegister(mUser, mPwd);
					ParseUser.logOut(); //prevent
					finishLogin(intent);
					Log.d(TAG, "mUser Found, finish login end");
					joinRoom(mUser, mPwd);
				}

			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			//showProgress(false);
		}

		private void finishLogin(Intent intent) {
			final String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			final String accountPassword = intent.getStringExtra(PARAM_USER_PASSWORD);
			final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
			String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

			if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
				// Creating the account on the device and setting the auth token we got
				// (Not setting the auth token will cause another call to the server to authenticate the user)
				mAccountManager.addAccountExplicitly(account, accountPassword, null);
				mAccountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);
			} else {
				mAccountManager.setPassword(account, accountPassword);
			}

			setAccountAuthenticatorResult(intent.getExtras());
			setResult(AccountAuthenticatorActivity.RESULT_OK, intent);

			finish();
		}


	}

	private int ChkUser(String mUserName)
	{
		int recNo;

		recNo = 0;

		Log.d(TAG, "ChkUser Start, username:" + mUserName + ",start");
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", mUserName);

		//q.setLimit(1);
		Log.d(TAG, "ChkUser Start, username:" + mUserName +",end");
		try {
			Log.d(TAG, "ChkUser count OK");
			recNo = query.count();
		}
		catch (  ParseException e) {
			Log.e(TAG,"Error found at ParseException:" + e.getMessage());
			recNo = -1;
		}
		catch (Exception e){
			Log.e(TAG,"Error found at General Exception:" + e.getMessage());
			recNo = -2; //error found here: null pointer exception 7-Sep
		}
		Log.d(TAG,"ChkUser quit at :"+recNo);
		return recNo;
	}

	private void DoRegister(String mUserName,String mUserPwd)
	{
		String sEmail="";
		final ParseUser pu = new ParseUser();
		Log.d(TAG,"Do Register Start, username:"+mUserName +",pwd:"+mUserPwd);
		sEmail = mUserName + "@test.com";
		sEmail = sEmail.replace(" ","."); //Temporary action, will be changed in next version
		Log.d(TAG, "Do Register Start, email:" + sEmail);
		pu.setEmail(sEmail); //user's registered e-mail address (if any)
		pu.setPassword(mUserPwd); //surname
		pu.setUsername(mUserName); //gc no
		pu.put("usertype", "guest");
		pu.put("fullname", mUserName);
		pu.put("fullname",mUserName); //Temporary action, will be changed in next version
		pu.put("IsUsing",false);
		pu.put("online", true);

		try
		{
			pu.signUp();
		}
		catch (Exception e){
			Log.d(TAG,"Exception found during register:" + e.toString());
		}

		/*pu.signUpInBackground(new SignUpCallback() {

			@Override
			public void done(ParseException e) {

				if (e == null) {
					UserList.user = pu;
					startActivity(new Intent(Login.this, UserList.class));
					setResult(RESULT_OK);
					finish();
				} else {
					Utils.showDialog(
							Login.this,
							getString(R.string.err_singup) + " "
									+ e.getMessage());
					e.printStackTrace();
				}
			}
		});*/
	}

	@Override
	public void onBackPressed() {
		setResult(AccountAuthenticatorActivity.RESULT_CANCELED);
		super.onBackPressed();
	}

	public void joinRoom(String UserName, String Password)
	{
		ParseUser.logInInBackground(UserName, Password, new LogInCallback() {

			@Override
			public void done(ParseUser pu, ParseException e) {
				// dia.dismiss();
				if (pu != null) {
					UserList.user = pu;
					startActivity(new Intent(Login.this, UserList.class));
					finish();
				}
			}
		});
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}
