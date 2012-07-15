package com.test.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
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
    public static void onCreate(Context context){
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession(context);
        mApi = new DropboxAPI<AndroidAuthSession>(session);

        checkAppKeySetup(context);
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
                Log.i(TAG, "Error authenticating", e);
                mLoggedIn = false;
            }
        }
    }
    
    /**
     * Logs out of dropbox
     * @param activity The activity
     */
    public static void logOut(Context context){
        if (mLoggedIn) {
            // Remove credentials from the session
            mApi.getSession().unlink();

            // Clear our stored keys
            clearKeys(context);
            
            mLoggedIn = false;
        }
    }
    
    /**
     * This is an async call to login to dropbox, it will start the dropbox activity
     * @param activity The activity
     */
    public static void logIn(Context context){
        try{
            mApi.getSession().startAuthentication(context.getApplicationContext());
        } catch (RuntimeException e){
            Log.e(TAG, "There is another application installed with the same Dropbox key");
        }
    }
    
    /**
     * Copies the specified file to local cache and returns the path where it can be accessed
     * @param file The filename to get, this will be referenced locally from the dropbox app directory
     * @return The path and filename or null
     */
    public static String getFile(String file){
        
        return null;
    }
    
    /**
     * Used to send the result of the file upload to ShiVa
     */
    public native static void putFileOverwriteResult(String filename, long bytes);
    
    /**
     * Writes a string to the specified file.  This is meant for fairly low content writes.
     * @param file The filename to write to, this file will be referenced locally from the dropbox app
     * directory
     * @param contents The content to write to the file
     */
    public static void putFileOverwrite(String filename, String content){
        
        try {
            
            InputStream is = new ByteArrayInputStream(content.getBytes());
            Entry entry = mApi.putFileOverwrite(filename, is, content.length(), null);
            putFileOverwriteResult(entry.fileName(), entry.bytes);
    
        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            Log.e(TAG, "This app wasn't authenticated properly");
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            Log.e(TAG, "This file is too big to upload");
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            Log.e(TAG, "Upload canceled");
        } catch (DropboxServerException e) {
            // Server-side exception.  
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                Log.e(TAG, "Unauthorized dropbox user");
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            Log.e(TAG, "Network error.  Try again.");
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            Log.e(TAG, "Dropbox error.  Try again.");
        } catch (DropboxException e) {
            // Unknown error
            Log.e(TAG, "Unknown error.  Try again.");
        }
    }
    
    /**
     * From here down, the methods are taken straight from the Dropbox DBRoulette example and modified
     * slightly to use class members.
     */
    
    private static AndroidAuthSession buildSession(Context context) {
        AppKeyPair appKeyPair = new AppKeyPair(mKey, mSecret);
        AndroidAuthSession session;

        String[] stored = getKeys(context);
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
    
    private static void checkAppKeySetup(Context context) {
        // Check to make sure that we have a valid app key
        if (mKey.startsWith("CHANGE") || mSecret.startsWith("CHANGE")) {
            Log.e(TAG, "You forgot to set your dropbox key + secret");
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + mKey;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = context.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            Log.e(TAG, "URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
        }
    }
    
    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private static String[] getKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
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
    
    private static void clearKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    
}
