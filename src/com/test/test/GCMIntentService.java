package com.test.test;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";
    
    private native void onGCMRegistered(String id);
    private native void onGCMUnregistered(String id);
    private native void onGCMMessageReceived(String message);
    private native void onGCMError(String error);
    
    @Override
    protected void onError(Context context, String errorId) {
        onGCMError(errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        /**
         * It's difficult to generalize which data should be sent to ShiVa since
         * we aren't dictating what the server is packing into the data variable.
         * 
         * Here, I'm assuming that the data JSON looks like:
         * "data": {"command": "blah"}
         * 
         * So I'm pulling out the "command" extra and passing that into ShiVa
         */
        onGCMMessageReceived(intent.getStringExtra("command"));
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        onGCMRegistered(regId);
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        onGCMUnregistered(regId);
    }
}
