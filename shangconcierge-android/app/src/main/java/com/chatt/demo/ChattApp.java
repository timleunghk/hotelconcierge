package com.chatt.demo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * The Class ChattApp is the Main Application class of this app. The onCreate
 * method of this class initializes the Parse.
 */
public class ChattApp extends Application
{

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
/*	@Override
	public void onCreate()
	{
		String sParseKeyID,sParseKeyPwd;

		sParseKeyID = getString(R.string.parse_keyid);
		sParseKeyPwd = getString(R.string.parse_password);

		super.onCreate();
		Parse.initialize(this,sParseKeyID,sParseKeyPwd);

	}*/

	@Override
	public void onCreate()
	{
		String sAppID;
		String sClientID;
		String sServer;

		sAppID = getString(R.string.parse_appid);
		sClientID = getString(R.string.parse_keyid);
		sServer =  getString(R.string.parse_server);

		super.onCreate();


try{
	Log.d("LOGIN","sAppID:" + sAppID + ",sClientID:" + sClientID + ",sServer:" + sServer);
	Parse.initialize(new Parse.Configuration.Builder(this)
			.applicationId(sAppID)
			.clientKey(sClientID)
			.server(sServer)
			.build());

//	Parse.initialize(new Parse.Configuration.Builder(this)
//			.applicationId(sAppID)
//			.server(sServer)
//			.build());
}
catch(Exception e)
{
	Log.e("LOGIN", "Error found:" + e.getMessage());
}





	}
}
