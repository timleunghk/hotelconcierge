package com.chatt.demo;

/**
 * Created by timothy.leung on 28/08/2015.
 *
 *
 *
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyServerAuthenticator implements IServerAuthenticator
{

    private static Map<String, String> mCredentialsRepo;

    static {
        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("503042284750", "chow");
        mCredentialsRepo = Collections.unmodifiableMap(credentials);
    }

    @Override
    public String signUp(String username, String password) {
        // TODO: register new user on the server and return its auth token
        return null;
    }

    @Override
    public String signIn(String username, String password) {
        String authToken = null;
        String stmpOut="";

        stmpOut = username + "_" + password;
/*

        String stmpOut="";     /* String authToken = null;
        String nameID="";
        final DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");

        SoapGCProfileLookup soapGCProfile = new SoapGCProfileLookup();
        nameID = soapGCProfile.GetNameIDByProfileLookup("ByGCLastName", lastname, "", gcnumber);

        SoapFetchGCProfile soapFetchGC = new SoapFetchGCProfile();
        stmpOut = soapFetchGC.FetchGCProfile(nameID,"KSL","KSL-FB","HKD");


        //Sorry, member not found.  Please search again.

        if (stmpOut.equalsIgnoreCase("Sorry, member not found.  Please search again."))
        {
            authToken = "normal_000000000000_"  + lastname;
        }
        else
        {
            authToken = stmpOut;
        }
*/
        authToken = stmpOut;
        return authToken;
    }
}
