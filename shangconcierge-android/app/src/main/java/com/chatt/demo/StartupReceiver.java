package com.chatt.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

    private static String TAG="StartUpReceiver";

    public StartupReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "INIT OK, shangconcierge start");

            //Start GPS Service when boot completed
            Log.d(TAG, "INIT OK, shangconcierge GPS Service start");

            Intent myIntent = new Intent(context, GPSService.class);
            context.startService(myIntent);
            //Start Call Service when boot completed (receive)
            Log.d(TAG, "INIT OK, shangconcierge Call Service start");
            Intent callIntent = new Intent(context,CallService.class);
            context.startService(callIntent);

        }

    }
}
