package com.chatt.demo;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.chatt.demo.model.Conversation;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class ActionReceiver extends BroadcastReceiver {


    private static final String TAG = "ActionReceiver";


    public ActionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String sMode,sInternalID;
        String sMsgID;

        Chat mChat;

        sMode = intent.getStringExtra("MODE");
        sInternalID = intent.getStringExtra("OBJECTID");

        sMsgID = intent.getStringExtra("MSGID");

        Log.v(TAG, "Notification Message arrived. Action:" + action + ",smode:" + sMode + ",sInternalID:"+sInternalID);
        Log.v(TAG, "Notification Message arrived. Action:" + action + ",smode:" + sMode + ",MSGID:" + sMsgID);
        if (action.equalsIgnoreCase("DoCancel"))
        {



            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(ClearNotificationService.NOTIFICATION_ID);

            if ((sMode !=null) && (sInternalID !=null))
            {
                if(sMode.equalsIgnoreCase(UIActivity.END))
                {
                    UpdateSessionID(sInternalID, sMode);
                }


            }

            if (sMode !=null)
            {
                if (sMode.equalsIgnoreCase("MSGUNREAD"))
                {
                    Log.v(TAG, "Start Save Status");
                    UpdateMsgRead(sMsgID);
                }
            }


            Log.v(TAG, "Notification Message cancelled");
        }


    }

    void UpdateSessionID(String sInternalID,String status)
    {

        ParseObject po = ParseObject.createWithoutData("CallSections", sInternalID);
        Log.d(TAG,"SessionID to be saved:" + status);
        po.put("Status", status);



        po.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Saved successfully.
                    Log.d(TAG, "Update session id done, start call here");
                } else {
                    Log.d(TAG, "Cannot update session id,cannot pickup call");
                }
            }
        });
    }

     void UpdateMsgRead(String sInternalID)
    {

        ParseObject po = ParseObject.createWithoutData("concierge", sInternalID);
        po.put("msgread",Conversation.STATUS_READ);



        po.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Saved successfully.
                    Log.d(TAG, "Update session id done, start call here");
                } else {
                    Log.d(TAG, "Cannot update session id,cannot pickup call");
                }
            }
        });
    }

}
