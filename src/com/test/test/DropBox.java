package com.test.test;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropBox {
    
    private static final String TAG = "ShiVaDropBox";
    
    private static DropboxAPI<AndroidAuthSession> mApi;
    private static boolean mLoggedIn;
    private static String mKey;
    private static String mSecret;
    
    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    
    public static void Init(String key, String secret){
        mKey = key;
        mSecret = secret;
    }
    
    /**
     * Call this in onCreate of your main activity if you are using dropbox
     * @param activity
     */
    public static void onCreate(Activity activity){
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession(activity);
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        checkAppKeySetup(activity);
    }
    
    /**
     * Call this in onResume of your main activity if you are using dropbox
     */
    public static void onResume(Activity activity){
        AndroidAuthSession session = mApi.getSession();

        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(activity, tokens.key, tokens.secret);
                mLoggedIn = true;
            } catch (IllegalStateException e) {
                Toast.makeText(activity, "Couldn't authenticate with Dropbox:" + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "Error authenticating", e);
                mLoggedIn = false;
            }
        }
    }
    
    /**
     * Logs out of dropbox
     * @param activity The activity
     */
    public static void logOut(Activity activity){
        if (mLoggedIn) {
            // Remove credentials from the session
            mApi.getSession().unlink();

            // Clear our stored keys
            clearKeys(activity);
            
            mLoggedIn = false;
        }
    }
    
    public static int logIn(Activity activity){
        mApi.getSession().startAuthentication(activity);
        
        if(mApi.getSession().isLinked()){
            mLoggedIn = true;
            return 1;
        }
        else{
            mLoggedIn = false;
            Toast.makeText(activity, "Unable to login to dropbox", Toast.LENGTH_LONG).show();
            return 0;
        }
    }
    
    /**
     * From here down, the methods are taken straight from the Dropbox DBRoulette example and modified
     * slightly to use class members.
     */
    
    private static AndroidAuthSession buildSession(Activity activity) {
        AppKeyPair appKeyPair = new AppKeyPair(mKey, mSecret);
        AndroidAuthSession session;

        String[] stored = getKeys(activity);
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private static void checkAppKeySetup(Activity activity) {
        // Check to make sure that we have a valid app key
        if (mKey.startsWith("CHANGE") || mSecret.startsWith("CHANGE")) {
            Toast.makeText(activity, "You must apply for an app key and secret from developers.dropbox.com, and add them to ProjectSettings.java", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + mKey;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = activity.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            Toast.makeText(activity, "URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme, Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private static String[] getKeys(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private static void storeKeys(Activity activity, String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = activity.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
    private static void clearKeys(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    
}
