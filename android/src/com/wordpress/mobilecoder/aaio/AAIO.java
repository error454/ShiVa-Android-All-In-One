package com.wordpress.mobilecoder.aaio;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class implements all of the Android All In One functions.  The base shiva class is then extended from this to inherit
 * the necessary lifecycle events for handling the addons.
 * @author zachary.burke@gmail.com
 *
 */
public class AAIO extends Activity {
    
    public static final String TAG = "AAIO";
    
    private static Context mActivity;
    private Handler mDropboxHandler;
    
    public static final int MSG_DROPBOX_LOGIN = 1;
    public static final int MSG_DROPBOX_LOGOUT = 2;
    public static final int MSG_DROPBOX_FILEWRITTEN = 3;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mActivity = getApplicationContext();
        
        if(ProjectSettings.UseDropboxAPI){
            //Create the handler that will receive messages from various dropbox worker threads.
            //Right now the only one that is used is MSG_DROPBOX_LOGOUT, but I figured others
            //may have need of doing this via handlers.
            mDropboxHandler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what){
                        case MSG_DROPBOX_LOGIN:
                            dropBoxLogin();
                            break;
                        case MSG_DROPBOX_LOGOUT:
                            dropBoxLogout();
                            break;
                        case MSG_DROPBOX_FILEWRITTEN:
                            Bundle b = msg.getData();
                            if(b != null){
                                String file = b.getString("file");
                                long bytes = b.getLong("size");
                                fileWritten(file, bytes);
                            }
                            break;
                    }
                    super.handleMessage(msg);
                }
            };
            DropBox.Init(mDropboxHandler, ProjectSettings.DROPBOX_KEY, ProjectSettings.DROPBOX_SECRET);
            DropBox.onCreate(getApplicationContext());
        }
        
        if(ProjectSettings.UseGoogleCloudMessaging){
            Log.i(TAG, "starting GCM");
            GCMRegistrar.checkDevice(this);
            
            final String regId = GCMRegistrar.getRegistrationId(this);
            if (regId.equals("")) {
                Log.i(TAG, "registration id was empty, registering");
                GCMRegistrar.register(this, ProjectSettings.GCM_PROJECT_ID);
            }
            else{
                Log.i(TAG, "registration id was not empty, forcing update");
                GCMIntentService.forceIDUpdate(this);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
         if(ProjectSettings.UseDropboxAPI){
             DropBox.onResume(this);
         }
    }
    
    //------------------------------------------------------------------
    // @@BEGIN_JNI_CALLBACK_METHODS@@
    // This set of methods is called by the JNI layer and dispatches the
    // call as necessary to the appropriate thread/class/method
    //------------------------------------------------------------------
    /**
     * Login to dropbox
     */
    public static void dropBoxLogin(){
        if(ProjectSettings.UseDropboxAPI)
            DropBox.logIn(mActivity);
    }
    
    /**
     * Logout of dropbox
     */
    public static void dropBoxLogout(){
        if(ProjectSettings.UseDropboxAPI)
            DropBox.logOut(mActivity);
    }
    
    /**
     * Write the contents of a string to the specified file in the dropbox app folder
     * @param file Filename
     * @param contents Contents to write
     */
    public static void dropBoxPutFileOverwrite(final String file, final String contents){
        if(ProjectSettings.UseDropboxAPI){
            
            //This involves network so we need to run on a seperate thread
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    DropBox.putFileOverwrite(file, contents);
                }
            });
            t.start();
        }
    }
    
    /**
     * Copy the file specified from the dropbox app folder to a temporary area accessible to shiva
     * @param file
     */
    public static void dropBoxGetFile(final String file){
        if(ProjectSettings.UseDropboxAPI){
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    DropBox.getFile(file);
                }
            };
            
            //This involves network so we need to run on a seperate thread
            Thread t = new Thread(r);
            t.start();
        }
    }
    
    /**
     * Override this if you need to know when files have been written
     * @param bytes
     */
    public void fileWritten(String file, long bytes){
        
    }
    //------------------------------------------------------------------
    // @@END_JNI_CALLBACK_METHODS@@   
    //------------------------------------------------------------------
}
