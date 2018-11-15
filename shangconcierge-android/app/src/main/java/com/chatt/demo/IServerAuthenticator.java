package com.chatt.demo;

/**
 * Created by timothy.leung on 28/08/2015.
 */
public interface IServerAuthenticator {


    /**
     * Tells the server to create the new user and return its auth token.
     * @param username
     * @param password
     * @return Access token
     */
    public String signUp (final String username, final String password);

    /**
     * Logs the user in and returns its auth token.
     * @param username
     * @param password
     * @return Access token
     */
    public String signIn (final String username, final String password);
}
