package com.chatt.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class GPSReceiver extends BroadcastReceiver {

    private static final int NOTIF_ID = 1;
    private static final String LOGTAG = "GPSReceiver";

    public GPSReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String locname="";
        String action="";

        Double latitude=0.00,longitude=0.00;
        locname = intent.getStringExtra("LOCNAME");

        intent.putExtra("NOTIFICATION_ID", NOTIF_ID);
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        intent.putExtra("LOCNAME", locname);

        Toast.makeText(context, "Welcome to " + locname, Toast.LENGTH_LONG).show();

        Vibrator vibrator = (Vibrator) context.getSystemService(
                Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }
}
