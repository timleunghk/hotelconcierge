package com.chatt.demo;


import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.chatt.demo.model.Conversation;
import com.chatt.demo.utils.Const;
import com.chatt.demo.utils.Utils;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;

import com.chatt.demo.Login;

import java.util.*;

import java.util.List;
import java.util.Timer;

public class CallService extends Service {

    private static String TAG = "CallService";
    private AccountManager mAccountManager;
    private AuthPreferences mAuthPreferences;
    private String authToken;

    private String  sFullName;

    private Context ctx;

    private static String PACKAGE_NAME;

    private static int NOTIF_ID = 1;

    private static Timer timer = new Timer();

    private Account account;

    private static ParseUser user;

    private UIActivity mUIActivity;

    public CallService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
        ctx = this;

    }

    private class mainTask extends TimerTask
    {
        private String mUserName, mPwd;
        boolean hvMsg;

        public void run()
        {
            Account accArray[];

            PACKAGE_NAME = getApplicationContext().getPackageName();

            Log.d(TAG, "From CallService ....on create, start");

            authToken = null;
            mAuthPreferences = new AuthPreferences(ctx);
            mAccountManager = AccountManager.get(ctx);
            accArray = mAccountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);

            String tmpStr[] = mAuthPreferences.getAuthToken().split("_");

            mUserName = tmpStr[0];
            mPwd = tmpStr[1];


            Log.d(TAG, "Main Run: mUserName:" + mUserName + ", password:" + mPwd);

            for (Account accItem : accArray) {
                if (PACKAGE_NAME.equalsIgnoreCase(accItem.type)) {
                    ParseUser.logOut();
                    ParseUser.logInInBackground(mUserName, mPwd, new LogInCallback() {
                        @Override
                        public void done(ParseUser pu, ParseException e) {
                            if (pu != null) {
                                UserList.user = pu;
                                Log.d(TAG, "handleIncomingCall after login -- start");
                                handleIncomingCall(mUserName);
                                handleUnreadMessage(mUserName);

                                Log.d(TAG, "handleIncomingCall after login -- end");
                            }
                        }
                    });


                }
            }



        }



        private void handleUnreadMessage(String UserName)
        {
            Log.d(TAG, "handleUnreadMessage start, username=" + UserName);
            ParseQuery<ParseObject> q = ParseQuery.getQuery("concierge");
            q.whereEqualTo("receiver",UserName);
            q.whereEqualTo("msgread", Conversation.STATUS_UNREAD);
            q.addDescendingOrder("createdAt");
            Log.d(TAG, "handleUnreadMessage start, query");
            q.findInBackground(new FindCallback<ParseObject>() {

                                   @Override
                                   public void done(List<ParseObject> li, ParseException e) {
                                   String sReceiverName, sSenderName, sObjectID;
                                   Integer iTotalMsg;
                                   if (li != null && li.size() > 0) {
                                       iTotalMsg = li.size();
                                       ParseObject po = li.get(li.size() - 1);
                                       sSenderName = po.getString("sender");
                                       sReceiverName = po.getString("receiver");
                                       sObjectID = po.getObjectId();
                                       Log.d(TAG, "Message arrived:" + sSenderName + ", ttlmsg:" + iTotalMsg.toString());

                                       notifyIncomingMsg(sObjectID,sSenderName, sReceiverName, iTotalMsg,sFullName);


                                    }
               }
           }
            );
        }



        private void handleIncomingCall(String UserName)
        {
            //handle incoming Video call (thru Opentok) from shang concierge staff

            Log.d(TAG, "handleIncomingCall run");

            ParseQuery<ParseObject> q = ParseQuery.getQuery("CallSections");
            q.whereEqualTo("Status", UIActivity.CALL);
            q.whereEqualTo("ReceiverID", UserName);
            q.addDescendingOrder("createdAt");
            q.setLimit(1);

            q.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> li, ParseException e) {
                    String internalID;
                    String caller;
                    String publisherToken, subscriberToken, sessionID;


                    if (li != null && li.size() > 0) {
                        for (int i = li.size() - 1; i >= 0; i--) {
                            ParseObject po = li.get(i);

                            internalID = po.getObjectId();//.getString("objectId");
                            caller = po.getString("CallerID");
                            publisherToken = po.getString("PublisherToken");
                            subscriberToken = po.getString("SubscriberToken");
                            sessionID = po.getString("SessionID");
                            Log.d(TAG, "internalID 1:" + internalID);
                            notifyIncomingCall(internalID, caller, publisherToken, subscriberToken, sessionID);
                        }
                    }
                }
            });
        }


        private void notifyIncomingMsg(String sObjID, String SenderName,String UserName, Integer TotalMsg,String sFullName)
        {
            String callNotifyAfterChange;
            String callNotify;
            String sUnReadMsg;

            callNotify = getResources().getString(R.string.new_message);
            Log.d(TAG, callNotify);
            callNotifyAfterChange = callNotify.replace("XXCaller", UserName).replace("XXMsgNo", TotalMsg.toString());


            NotificationManager notiMgr = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();

            String[] events = callNotifyAfterChange.split("\n");

            inboxStyle.setBigContentTitle(getString(R.string.app_name));

            for (int i = 0; i < events.length; i++)
            {
                inboxStyle.addLine(events[i]);
            }


            NotificationCompat.Builder noti =
                    new NotificationCompat.Builder(ctx)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(callNotifyAfterChange)
                            .setStyle(inboxStyle);

            //Should go to Chat.java ; direct call from Concierge staff

           // Intent mIntent = new Intent(ctx, com.chatt.demo.Chat.class);
            Intent mIntent = new Intent(ctx, com.chatt.demo.UserList.class);
            mIntent.putExtra("NOTIFICATION_ID", NOTIF_ID);
            mIntent.putExtra("sendername",UserName);
            mIntent.putExtra("receivername",SenderName);
            mIntent.putExtra("receiverfullname",sFullName);


            Intent iCancel = new Intent(ctx, com.chatt.demo.ActionReceiver.class);

            iCancel.putExtra("MODE", "MSGUNREAD");
            iCancel.putExtra("MSGID", sObjID);
            iCancel.setAction("DoCancel");


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
           // stackBuilder.addParentStack(com.chatt.demo.UIActivity.class);
            stackBuilder.addParentStack(com.chatt.demo.UserList.class);
            stackBuilder.addNextIntent(mIntent);

            PendingIntent pIntent =
                    stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            noti.setContentIntent(pIntent);

            PendingIntent pCancel = PendingIntent.getBroadcast(ctx.getApplicationContext(), 0, iCancel, PendingIntent.FLAG_UPDATE_CURRENT);

            noti.addAction(R.drawable.ic_concierge_call, getString(R.string.location_notify_yes), pIntent);
            noti.addAction(R.drawable.ic_concierge_cancel, getString(R.string.location_notify_no), pCancel);

            notiMgr.notify(NOTIF_ID, noti.build());

        }

        private void notifyIncomingCall(String internalID, String caller, String pubToken, String subToken, String sessionID)
        {
            String callNotifyAfterChange;
            String callNotify;

            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);


            NotificationManager notiMgr = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);

            callNotify = getResources().getString(R.string.call_notify);
            Log.d(TAG, callNotify);
            callNotifyAfterChange = callNotify.replace("XXCaller", caller);


            NotificationCompat.BigTextStyle bigTextStyle =
                    new NotificationCompat.BigTextStyle();

            String[] events = callNotifyAfterChange.split("\n");

            bigTextStyle.setBigContentTitle(getString(R.string.app_name));





            NotificationCompat.Builder noti = new NotificationCompat.Builder(ctx);
            noti.setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(String.format(callNotifyAfterChange, getString(R.string.app_name)))
                    .setDefaults(Notification.DEFAULT_ALL) //sound + vibrate
                    .setStyle(bigTextStyle)
                    .setPriority(Notification.PRIORITY_HIGH);



            Intent mIntent = new Intent(ctx, com.chatt.demo.UIActivity.class);
            mIntent.putExtra("NOTIFICATION_ID", NOTIF_ID);
            mIntent.putExtra("MODE", UIActivity.CALL);
            mIntent.putExtra("SESSIONID", sessionID);
            mIntent.putExtra("PUBTOKEN", pubToken);
            mIntent.putExtra("SUBTOKEN", subToken);
            mIntent.putExtra("OBJECTID", internalID);
            mIntent.putExtra("MYNAME",caller);
            mIntent.putExtra("PLAYMUSIC", "No");

            Intent iCancel = new Intent(ctx, com.chatt.demo.ActionReceiver.class);
            iCancel.putExtra("MODE", UIActivity.END);
            iCancel.putExtra("OBJECTID", internalID);
            iCancel.setAction("DoCancel");


            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            stackBuilder.addParentStack(com.chatt.demo.UIActivity.class);
            stackBuilder.addNextIntent(mIntent);

            PendingIntent pIntent =
                    stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            noti.setContentIntent(pIntent);


            Log.d(TAG, "Receive Call, caller ID:"+ caller  +" ObjectID:" + internalID + ",SessionID:" + sessionID + ", PubToken:" + pubToken + ", SubToken:" + subToken);

            PendingIntent pCancel = PendingIntent.getBroadcast(ctx.getApplicationContext(), 0, iCancel, PendingIntent.FLAG_UPDATE_CURRENT);



            //    noti.setSound(uri);
            noti.addAction(R.drawable.ic_concierge_call, getString(R.string.location_notify_yes), pIntent);
            noti.addAction(R.drawable.ic_concierge_cancel, getString(R.string.location_notify_no), pCancel);

            notiMgr.notify(NOTIF_ID, noti.build());
        }


    }



    public void onDestroy()
    {
        super.onDestroy();
    }



}
