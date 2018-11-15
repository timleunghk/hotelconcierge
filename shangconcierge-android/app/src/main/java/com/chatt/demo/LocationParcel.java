package com.chatt.demo;

/**
 * Created by timothy.leung on 20/08/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;



public class LocationParcel implements Parcelable
{
    /*
  <location>
      <type>hotel</type>
      <name>Kowloon Shangri-la</name>
      <area>Kowloon</area>
      <city>Hong Kong</city>
      <country>Hong Kong</country>
      <lat>22.297198</lat>
      <long>114.177361</long>
  </location>
  */
    private static final String LOGTAG = "LocationParcel";

    private LocationInfo locInfo;

   /* private String loctype=null;
    private String locname=null;
    private String locarea=null;
    private String loccity=null;
    private String loccountry=null;
    private Double loclat=0.00;
    private Double loclong=0.00;*/

    public LocationParcel()
    {
        super();
    }


    public LocationParcel(LocationInfo locInfo)
    {
        super();
        this.locInfo = locInfo;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(locInfo.getLocType());
        dest.writeString(locInfo.getLocName());
        dest.writeString(locInfo.getLocArea());
        dest.writeString(locInfo.getLocCity());
        dest.writeString(locInfo.getLocCountry());
        dest.writeDouble(locInfo.getLoclat());
        dest.writeDouble(locInfo.getLoclong());
    }

    private void readFromParcel(Parcel in)
    {

    }

    public LocationParcel(Parcel in){
        locInfo = new LocationInfo();
        locInfo.setLocType(in.readString());
        locInfo.setLocName(in.readString());
        locInfo.setLocArea(in.readString());
        locInfo.setLocCity(in.readString());
        locInfo.setLocCountry(in.readString());
        locInfo.setLoclat(in.readDouble());
        locInfo.setLoclong(in.readDouble());
    }



    public LocationInfo getLocInfo()
    {
        return locInfo;
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
