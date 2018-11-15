package com.chatt.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatt.demo.custom.CustomActivity;
import com.chatt.demo.model.Conversation;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Utils;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * The Class UserList is the Activity class. It shows a list of all users of
 * this app. It also shows the Offline/Online status of users.
 */
public class UserList extends CustomActivity
{

	/** The Chat list. */
	private ArrayList<ParseUser> uList;

	/** The user. */
	public static ParseUser user;

	public static final String LOG_USERLIST = "UserList";

	private int messageNo;

	public static String sUser;


	private static int NOTIF_ID = 1;

    private ParseUser c ;//= getItem(pos);
    private TextView lbl;// = (TextView) v;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);



		setContentView(R.layout.user_list);
		getActionBar().setDisplayHomeAsUpEnabled(false);

		findViewById(R.id.btnTakeMeBack).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						takeMeBack();
					}
				});
		loadUserList();

	//	updateUserStatus(true);

		/*
		*  mIntent.putExtra("sendername",SenderName);
            mIntent.putExtra("receivername",UserName);
		* */
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(LOG_USERLIST, "onConfigurationChanged triggered");
		int orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		setRequestedOrientation(orientation);
	}

	private void takeMeBack()
	{
		startActivity(new Intent(UserList.this, Map.class));
		//finish();
	}


	@Override
	protected void onStop() {
		super.onStop();
//		updateUserStatus(false);
		Log.d(LOG_USERLIST, "User List Stopped sub is triggered");
	}

	@Override
	protected void onRestart()
	{
		super.onRestart();
//		updateUserStatus(true);
		Log.d(LOG_USERLIST, "User List Restarted sub is triggered");
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	//	updateUserStatus(false);

		Log.d(LOG_USERLIST,"User List Destroy sub is triggered");
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		loadUserList();

	//	Log.d(LOG_USERLIST, "User List Destroy sub is resumed");

	}

	/**
	 * Update user status.
	 * 
	 * @param online
	 *            true if user is online
	 */
	private void updateUserStatus(boolean online)
	{
//		user.put("online", online);
//		user.saveEventually();
	}

	private void loadUserList()
	{
		String sTarget;
		Log.d(LOG_USERLIST,"Load User List Start");
		Log.d(LOG_USERLIST,"Load User List Get User Name");
		sUser =  user.getUsername();
		if ((sUser != null) && (sUser.length()!=0))
		{
			Log.d(LOG_USERLIST,"Load User List Get User Name Done, user name (fullname) value:" + user.get("fullname").toString());

			final ProgressDialog dia = ProgressDialog.show(this, null,
					getString(R.string.alert_loading));

			Log.d(LOG_USERLIST, "Load User List Get User Name Done, user type value:" + user.get("usertype").toString());


			if (user.get("usertype").toString().equalsIgnoreCase("guest"))
			{
				sTarget = "staff";
			}
			else // if usertype is staff, load guest info
			{
				sTarget = "guest";
			}
			Log.d(LOG_USERLIST, "Load User List GetQuery Start, target="+sTarget);
			ParseUser.getQuery()
					.whereEqualTo("usertype", sTarget)
					.whereNotEqualTo("username", sUser)
					.findInBackground(new FindCallback<ParseUser>() {

						@Override
						public void done(List<ParseUser> li, ParseException e) {
							dia.dismiss();
							Log.d(LOG_USERLIST, "Load User List GetQuery Done");
							if (li != null) {
								if (li.size() == 0)
									Toast.makeText(UserList.this,
											R.string.msg_no_user_found,
											Toast.LENGTH_SHORT).show();

								//Show online user here.
								Log.d(LOG_USERLIST, "Load User List GetQuery Show List");
								uList = new ArrayList<ParseUser>(li);
								ListView list = (ListView) findViewById(R.id.list);
								Log.d(LOG_USERLIST, "Load User List GetQuery Show List .. created ulist");
								list.setAdapter(new UserAdapter());
								Log.d(LOG_USERLIST, "Load User List GetQuery Show List .. created set adapter done");
								list.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(AdapterView<?> arg0,
															View arg1, int pos, long arg3) {
										Intent mIntent = new Intent(UserList.this, com.chatt.demo.Chat.class);
										mIntent.putExtra("NOTIFICATION_ID", NOTIF_ID);
										mIntent.putExtra("sendername", user.getUsername());
										mIntent.putExtra("receivername", uList.get(pos).getUsername());
										mIntent.putExtra("receiverfullname", uList.get(pos).getString("fullname"));
										if (uList.get(pos).getBoolean("online")==true){
											mIntent.putExtra("status","online");
										}
										else
										{
											mIntent.putExtra("status","offline");
										}

										Log.d(LOG_USERLIST, "User List selected user: sendername: " + user.getUsername() + ", receivername:" + uList.get(pos).getUsername() + ", receiver fullname: " + uList.get(pos).getString("fullname"));


										startActivity(mIntent);

									}
								});
							} else {
								Log.e(LOG_USERLIST, "Load User List GetQuery Show List Error Found:" + e.getMessage());
								Utils.showDialog(
										UserList.this,
										getString(R.string.err_users) + " "
												+ e.getMessage());
								e.printStackTrace();
							}
						}
					});
		}
		else
		{
			Log.d(LOG_USERLIST,"Load User List end with sUser is null");
		}
	}



	/**
	 * The Class UserAdapter is the adapter class for User ListView. This
	 * adapter shows the user name and it's only online status for each item.
	 */
	private class UserAdapter extends BaseAdapter
	{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return uList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public ParseUser getItem(int arg0)
		{
			return uList.get(arg0);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int arg0)
		{
			return arg0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#stgetView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int pos, View v, ViewGroup arg2)
		{


            if (v == null)
				v = getLayoutInflater().inflate(R.layout.chat_item, null);

		    c = getItem(pos);
		    lbl = (TextView) v;

            ParseQuery<ParseObject> q = ParseQuery.getQuery("concierge");
            q.whereEqualTo("sender", c.getString("fullname"));
            q.whereEqualTo("receiver", user.getUsername());
            q.whereGreaterThan("updatedAt", user.getUpdatedAt());
            q.setLimit(1);



           Boolean hvMsg;
            hvMsg = false;

			if (c.getBoolean("online"))
			{
				lbl.setText(c.getString("fullname") + "(Online)");
			}
			else
			{
				lbl.setText(c.getString("fullname") + "(Offline)");
			}



            hvMsg = bHaveNewMessage(c.getString("username"), user.getUsername(), user.getUpdatedAt());

			lbl.setCompoundDrawablesWithIntrinsicBounds(
					hvMsg ? R.drawable.ic_online
							: R.drawable.ic_offline, 0, R.drawable.arrow, 0);

            return v;
		}




		private boolean bHaveNewMessage(String sTarget,String myUserName,Date lastUpdateDateTime)
		{
			boolean tmp;

			tmp=false;

			ParseQuery<ParseObject> q = ParseQuery.getQuery("concierge");
			q.whereEqualTo("sender", sTarget);
			q.whereEqualTo("receiver", myUserName);
			q.whereGreaterThan("updatedAt", lastUpdateDateTime);
			q.setLimit(1);

			try {
				messageNo = q.count();
			}
			catch (  ParseException e) {
				messageNo = 0;
			}

			Log.d(LOG_USERLIST, "Sender: " + sTarget + ",receiver:" + myUserName + ", Total message main exit:" + messageNo);


			if (messageNo>0)
			{
				tmp = true;
			}
			else
			{
				tmp = false;
			}
			return tmp;
		}



	}
}
