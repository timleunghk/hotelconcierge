package com.chatt.demo;

import java.io.Serializable;
/**
 * Created by timothy.leung on 20/08/2015.
 */
public class LocationInfo
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
     private static final String LOGTAG = "LocationInfo";


    String loctype=null;
    String locname=null;
    String locarea=null;
    String loccity=null;
    String loccountry=null;
    Double loclat=0.00;
    Double loclong=0.00;

    public String getLocType() {
        return loctype;
    }
    public void setLocType(String loctype) {
        this.loctype = loctype;
    }

    public String getLocName() {
        return locname;
    }
    public void setLocName(String locname) {
        this.locname = locname;
    }

    public String getLocArea() {
        return locarea;
    }
    public void setLocArea(String locarea) {
        this.locarea = locarea;
    }

    public String getLocCity() {
        return loccity;
    }
    public void setLocCity(String loccity) {
        this.loccity = loccity;
    }

    public String getLocCountry() {
        return loccountry;
    }
    public void setLocCountry(String loccountry) {
        this.loccountry = loccountry;
    }


    public Double getLoclat() {
        return loclat;
    }
    public void setLoclat(Double loclat) {
        this.loclat = loclat;
    }

    public Double getLoclong() {
        return loclong;
    }
    public void setLoclong(Double loclong) {
        this.loclong = loclong;
    }

}
