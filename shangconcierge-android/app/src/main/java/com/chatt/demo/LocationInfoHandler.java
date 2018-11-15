package com.chatt.demo;

/**
 * Created by timothy.leung on 20/08/2015.
 */

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;


public class LocationInfoHandler  extends DefaultHandler
{
    private static final String LOGTAG = "LocationInfoHandler";

    boolean currentElement = false;
    String currentValue = "";

    Double currentDblValue = 0.00;

    String loctype=null;
    String locname=null;
    String locarea=null;
    String loccity=null;
    String loccountry=null;
    Double loclat=0.00;
    Double loclong=0.00;

    LocationInfo locInfo;
    ArrayList<LocationInfo> locInfoList;

    public String getLocType() {
        return loctype;
    }
    public String getLocName() {
        return locname;
    }
    public String getLocArea() {
        return locarea;
    }
    public String getLocCity() {
        return loccity;
    }
    public String getLocCountry() {
        return loccountry;
    }
    public Double getLoclat() {
        return loclat;
    }
    public Double getLoclong() {
        return loclong;
    }

    public ArrayList<LocationInfo> getLocinfoList()
    {
        return locInfoList;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException
    {

        currentElement = true;

        if (qName.equals("LocList"))
        {
            locInfoList = new ArrayList<LocationInfo>();
        }
        else if (qName.equals("location"))
        {
            locInfo = new LocationInfo();
        }

    }

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
    *
    * */

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        currentElement = false;

        if (qName.equalsIgnoreCase("type"))
        {
            locInfo.setLocType(currentValue.trim());
        }
        else if (qName.equalsIgnoreCase("name"))
        {
            locInfo.setLocName(currentValue.trim());
        }
        else if (qName.equalsIgnoreCase("area"))
        {
            locInfo.setLocArea(currentValue.trim());
        }
        else if (qName.equalsIgnoreCase("city"))
        {
            locInfo.setLocCity(currentValue.trim());
        }
        else if (qName.equalsIgnoreCase("country"))
        {
            locInfo.setLocCountry(currentValue.trim());
        }
        else if (qName.equalsIgnoreCase("lat"))
        {
            currentDblValue = Double.valueOf(currentValue.trim()).doubleValue();
            locInfo.setLoclat(currentDblValue);
        }
        else if (qName.equalsIgnoreCase("long"))
        {
            currentDblValue = Double.valueOf(currentValue.trim()).doubleValue();
            locInfo.setLoclong(currentDblValue);
        }
        else if (qName.equalsIgnoreCase("location"))
        {
            locInfoList.add(locInfo);
        }
        currentValue = "";

    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (currentElement) {
            currentValue = currentValue + new String(ch, start, length);
        }

    }
}
