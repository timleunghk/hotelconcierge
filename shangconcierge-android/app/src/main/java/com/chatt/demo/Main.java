package com.chatt.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Display;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.util.Log;
import android.view.Surface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class Main extends Activity {

    private static final String TAG = "MainActivity";
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    private int mNaturalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;


    private static final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private AuthPreferences mAuthPreferences;
    private String authToken;
    private  Configuration newConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        if (checkPlayServices())
        {
            if (checkGoogleMap())
            {
                authToken = null;
                mAuthPreferences = new AuthPreferences(this);
                mAccountManager = AccountManager.get(this);

                //account check

                Log.d(TAG,"Start auth.");
                mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new GetAuthTokenCallback(), null);
                Log.d(TAG, "End auth.");

                Intent intent = new Intent(this, GPSService.class);
                startService(intent);
            }
            else //if google map not found
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.install_gmap);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.install_gmap_reply, getGoogleMapsListener());
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
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




    private DialogInterface.OnClickListener getGoogleMapsListener()
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));

        //android 5 may ok
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                startActivity(intent);

                //Finish the activity so they can't circumvent the check
                finish();
            }
        };
    }

    private boolean checkPlayServices(){
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }


    private boolean checkGoogleMap()
    {

        try
        {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            return false;
        }

    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
        private  String mUserName,mPwd;

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    startActivityForResult(intent, REQ_SIGNUP);
                } else {
                    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);

                    // Save session username & auth token
                    mAuthPreferences.setAuthToken(authToken);
                    mAuthPreferences.setUsername(accountName);

                    //text3.setText("Saved auth token: " + mAuthPreferences.getAuthToken());
                    String tmpStr[] = mAuthPreferences.getAuthToken().split("_");
                    Account account = AccountUtils.getAccount(Main.this, accountName);
                    //Account account = AccountUtils.getAccount(MainActivity.this, accountName);
                    if (null == account) {
                        account = new Account(accountName, AccountUtils.ACCOUNT_TYPE);
                        //mAccountManager.addAccountExplicitly(account, bundle.getString(LoginActivity.PARAM_USER_PASSWORD), null);
                        mAccountManager.addAccountExplicitly(account, bundle.getString(Login.PARAM_USER_PASSWORD), null);
                        mAccountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);
                    }
                    mUserName = tmpStr[0];
                    mPwd = tmpStr[1];

                    Log.d(TAG,"AccountManagerFuture result mUserName:" + mUserName + ", password:" + mPwd);


                    joinRoom(mUserName,mPwd);





                }
            } catch(OperationCanceledException e) {
                // If signup was cancelled, force activity termination
                finish();
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void joinRoom(String UserName, String Password)
    {
        ParseUser.logInInBackground(UserName, Password, new LogInCallback() {

            @Override
            public void done(ParseUser pu, ParseException e) {
               // dia.dismiss();
                if (pu != null) {
                    UserList.user = pu;
                    startActivity(new Intent(Main.this, UserList.class));
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
