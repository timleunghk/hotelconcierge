package com.chatt.demo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;


import com.chatt.demo.utils.Directions;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class Map extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<LocationParcel> locationParcelList;
    private LocationInfo locInfo;
    private double latitude, longitude;
    private double curlat,curlong;
    private String locname="";

    private  Location curLoc;
    private LatLng latlngStart,latlngEnd;
    private ArrayList<LatLng> markerpoints;

    private static String LOG = "Map";

    void setupActionBar()
    {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null)
            return;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.actionbar_bg));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setupActionBar();


        setContentView(R.layout.activity_map);

  //      String provider = LocationManager.NETWORK_PROVIDER;

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); // Choose your accuracy requirement.


        String provider = locationManager.getBestProvider(criteria, true);

        Log.d(LOG, "Selected provider in Google Map:" + provider);

        curLoc = locationManager.getLastKnownLocation(provider);

        locationManager.requestLocationUpdates(provider, 1000, 1, locationListener);

        // Log.d(LOG, "curLoc is not null");

        updateMyCurrentLoc(curLoc);
        if (curLoc!=null)
        {
            curlat = curLoc.getLatitude();
            Log.d(LOG, "Get curLoc.getLatitude...End");
            Log.d(LOG, "Get curLoc.getLongitude...Start");
            curlong = curLoc.getLongitude();
            Log.d(LOG, "Get curLoc.getLongitude...End");
            latlngStart = new LatLng(curlat, curlong);


            setUpMapIfNeeded();

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setBuildingsEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }


    }



    @Override
    protected void onResume() {
        super.onResume();

        updateMyCurrentLoc(curLoc);

        if (curLoc!=null)
        {
            setUpMapIfNeeded();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setBuildingsEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

        }
  /*      else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.no_take_me_home);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.install_gmap_reply, getQuitListener());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
*/

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        String sCurrentStayingHotel;

        sCurrentStayingHotel=getString(R.string.test_staying_hotel); //test: KSL
        locationParcelList = this.loadLocXML();

        //Show Destination (i.e: KSL,ISL,HJHK.....)
        for (LocationParcel locationParcel : locationParcelList) {
            locInfo = locationParcel.getLocInfo();
            longitude = locInfo.getLoclong();
            latitude = locInfo.getLoclat();
            locname = locInfo.getLocName();
            if (sCurrentStayingHotel.equalsIgnoreCase(locname))
            {
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(locname));
                mMap.addMarker(new MarkerOptions().position(new LatLng(curlat, curlong)).title("You are here"));
                latlngEnd = new LatLng(latitude, longitude);
            }


            //camera
            CameraPosition cameraPos = new CameraPosition.Builder().target(latlngStart).zoom(16.0f).build();
            CameraUpdate cameraUpt = CameraUpdateFactory.newCameraPosition(cameraPos);
            mMap.moveCamera(cameraUpt);

        }
        Directions.getInstance().draw(this, latlngStart, latlngEnd, mMap, Directions.MODE_DRIVING);
      //  Directions.getInstance().draw(this, latlngStart, latlngEnd, mMap, Directions.MODE_TRANSIT);
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

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateMyCurrentLoc(location);
        }

        public void onProviderDisabled(String provider){
            updateMyCurrentLoc(null);
        }

        public void onProviderEnabled(String provider){ }
        public void onStatusChanged(String provider, int status,
                                    Bundle extras){ }
    };


    private void updateMyCurrentLoc(Location location) {

        if (location!=null)
        {
            curlat = location.getLatitude();
            Log.d(LOG, "Get curLoc.getLatitude...End");
            Log.d(LOG, "Get curLoc.getLongitude...Start");
            curlong = location.getLongitude();
            Log.d(LOG, "Get curLoc.getLongitude...End");
            latlngStart = new LatLng(curlat, curlong);


            setUpMapIfNeeded();

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setBuildingsEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }


    }

    private DialogInterface.OnClickListener getQuitListener()
    {
        return new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                Intent intent = new Intent(Map.this,UserList.class);
                startActivity(intent);


                finish();
            }
        };
    }
}
