package com.chatt.demo;

/**
 * Created by timothy.leung on 28/08/2015.
 */
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;


public class AccountUtils {

    public static final String ACCOUNT_TYPE = "com.chatt.demo";
    public static final String AUTH_TOKEN_TYPE = "com.chatt.demo.authentication";

    public static IServerAuthenticator mServerAuthenticator = new MyServerAuthenticator();

    public static Account getAccount(Context context, String accountName) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName)) {
                return account;
            }
        }
        return null;
    }

}



