package com.test.test;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";
    
    @Override
    protected void onError(Context context, String errorId) {
        Log.e(TAG, "Error: " + errorId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Got message! " + intent.toString());
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        Log.i(TAG, "Registered device: " + regId);
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.i(TAG, "Unregistered device: " + regId);
    }
}
