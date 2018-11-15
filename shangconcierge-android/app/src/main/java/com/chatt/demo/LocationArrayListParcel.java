package com.chatt.demo;

/**
 * Created by timothy.leung on 20/08/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class LocationArrayListParcel implements Parcelable
{
    private static final String LOGTAG = "LocationArrayListParcel";

    private ArrayList<LocationParcel> locParList = new ArrayList<LocationParcel>();


    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeTypedList(locParList);
    }

    private void readFromParcel(Parcel in) {

    }

    public LocationArrayListParcel()
    {

    }
    public ArrayList<LocationParcel> getArrayList()
    {
        return locParList;
    }


    public LocationArrayListParcel(Parcel in)
    {
        this.locParList = new ArrayList<LocationParcel>();
        in.readTypedList(locParList,LocationParcel.CREATOR);
    }

    public void addLocationList(LocationParcel locationParcel)
    {
        locParList.add(locationParcel);

    }



    public static final Parcelable.Creator<LocationParcel> CREATOR = new Parcelable.Creator<LocationParcel>() {

        @Override
        public LocationParcel createFromParcel(Parcel source) {
            return new LocationParcel(source);
        }

        @Override
        public LocationParcel[] newArray(int size) {
            return new LocationParcel[size];
        }

    };
}
