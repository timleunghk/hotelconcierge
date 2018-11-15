package com.chatt.demo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.res.AssetManager;

import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GPSService extends Service implements LocationListener {


    private LocationManager manager;
    private boolean isInArea;
    private double latitude, longitude;
    private String locname="";
    private String prevlocname="";
    private static final int NOTIF_ID = 1;
    private ArrayList<LocationParcel> locationParcelList;
    private Intent mIntent;
    private LocationInfo locInfo;
    private String countryname = "";

    private Geocoder code;

    String locNotify;


    private static final String LOGTAG = "GPSService";





    public GPSService()
    {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
       // String bestProvider = LocationManager.NETWORK_PROVIDER;

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_COARSE ); // Choose your accuracy requirement.

        String bestProvider = manager.getBestProvider(criteria, true);
        Log.d(LOGTAG, "Selected best GPS provider:" + bestProvider);
        manager.requestLocationUpdates(bestProvider, 1000, 1, this);


        isInArea = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        locationParcelList = this.loadLocXML();
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  manager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location current)
    {
        Double latDbl,longDbl;
        float mindist=0.00f;
        Location dest;

        Integer iListSize,iCount;


        dest = new Location(current);
        mindist = Float.valueOf(getResources().getString(R.string.alertdist));

        iListSize = locationParcelList.size();

        Log.d(LOGTAG, "Total Read size of localParcelList: " + iListSize.toString());

        iCount = 0;


        for (LocationParcel locationParcel : locationParcelList) {
            locInfo = locationParcel.getLocInfo();
            longitude = locInfo.getLoclong();
            latitude = locInfo.getLoclat();
            locname = locInfo.getLocName();


            dest.setLatitude(latitude);
            dest.setLongitude(longitude);
            float distance = current.distanceTo(dest);
            updateLocationDistance(locname, distance); //logging
            Log.d(LOGTAG, "LIST: item no: "+iCount.toString()+",locname:" + locname + " prevlocname=" +prevlocname +",Distance (m): " + distance);
            if (distance <= mindist)
            { //if current position
                if (locname.equalsIgnoreCase(prevlocname)==false)
                {
                    Log.d("GPSService", "within distance locname:" + locname + " prevlocname=" +prevlocname);
                    Intent intent = new Intent("android.broadcast.LOCATION");
                    intent.putExtra("LATITUDE", latitude);
                    intent.putExtra("LONGITUDE", longitude);
                    intent.putExtra("LOCNAME", locname);
                    sendBroadcast(intent);
                    notifyLocationChanged(locname);
                    prevlocname = locname;
                }
                break;
            }
            else
            {
                iCount++;
            }

        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
    @Override
    public void onProviderEnabled(String provider)
    {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    private void updateLocationDistance(String loc,float distance)
    {
        String tmp;
        tmp= loc + " Distance (m):" + String.valueOf(distance) + "\r\n";
        //  Log.d("GPSService-updlocdist",tmp);
    }

    private void notifyLocationChanged(String loc)
    {
        String locNotifyAfterChange;

        String CUSTOM_INTENT="1";

        locNotify = getResources().getString(R.string.location_notify);
        Log.d(LOGTAG, locNotify);
        locNotifyAfterChange = locNotify.replace("XXLoc",loc);

        Log.d(LOGTAG,locNotifyAfterChange);

        NotificationManager notiMgr = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        String[] events = locNotifyAfterChange.split("\n");

        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        for (int i=0; i < events.length; i++) {
            inboxStyle.addLine(events[i]);
        }


        NotificationCompat.Builder noti =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(locNotifyAfterChange)
                        .setStyle(inboxStyle);



        Intent mIntent = new Intent(this, com.chatt.demo.Main.class);
        mIntent.putExtra("NOTIFICATION_ID", NOTIF_ID);

        Intent iCancel = new Intent (this, com.chatt.demo.ActionReceiver.class);
        iCancel.setAction("DoCancel");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(com.chatt.demo.Main.class);
        stackBuilder.addNextIntent(mIntent);

        PendingIntent pIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        noti.setContentIntent(pIntent);


        PendingIntent pCancel = PendingIntent.getBroadcast(this.getApplicationContext(), 0, iCancel, PendingIntent.FLAG_UPDATE_CURRENT);

        noti.addAction(R.drawable.ic_concierge_call, getString(R.string.location_notify_yes), pIntent);
        noti.addAction(R.drawable.ic_concierge_cancel, getString(R.string.location_notify_no), pCancel);

        notiMgr.notify(NOTIF_ID, noti.build());
    }

    private ArrayList<LocationParcel> loadLocXML()
    {
        AssetManager assetManager;
        LocationParcel locationParcel;
        LocationArrayListParcel locationArrayListParcel;
        try
        {
            assetManager = getBaseContext().getAssets();
            InputStream is = assetManager.open("location.xml");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            LocationInfoHandler locationHandler = new LocationInfoHandler();
            xr.setContentHandler(locationHandler);
            InputSource inStream = new InputSource(is);
            xr.parse(inStream);

            ArrayList<LocationInfo> locInfoList = locationHandler.getLocinfoList();
            locationArrayListParcel = new LocationArrayListParcel();
            for(LocationInfo locInfo:locInfoList )
            {
                locationParcel = new LocationParcel(locInfo);
                locationArrayListParcel.addLocationList(locationParcel);
            }
            ArrayList<LocationParcel> locationParcelArrayList = locationArrayListParcel.getArrayList();
            return locationParcelArrayList;

        }
        catch (Exception e)
        {
            return null;
        }
    }

}
