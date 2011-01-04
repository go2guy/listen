package com.interact.listen.android.voicemail.sync;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;

final class CloudRegistration
{
    private static final String TAG = Constants.TAG + "CloudReg";
    
    private static final String EXTRA_SENDER = "sender";
    private static final String EXTRA_APPLICATION_PENDING_INTENT = "app";
    
    private static final String REQUEST_UNREGISTRATION_INTENT = "com.google.android.c2dm.intent.UNREGISTER";
    private static final String REQUEST_REGISTRATION_INTENT = "com.google.android.c2dm.intent.REGISTER";

    private static final String GSF_PACKAGE = "com.google.android.gsf";
    
    static void register(Context context, String senderId)
    {
        if(TextUtils.isEmpty(senderId))
        {
            Log.d(TAG, "sender ID not set");
            return;
        }
        
        Intent regIntent = new Intent(REQUEST_REGISTRATION_INTENT);
        regIntent.setPackage(GSF_PACKAGE);
        regIntent.putExtra(EXTRA_APPLICATION_PENDING_INTENT, PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        regIntent.putExtra(EXTRA_SENDER, senderId);

        if(context.startService(regIntent) == null)
        {
            Log.e(TAG, "C2DM service not found!");
        }
        else
        {
            Log.i(TAG, "C2DM register service started");
        }
    }

    static void unregister(Context context)
    {
        Intent regIntent = new Intent(REQUEST_UNREGISTRATION_INTENT);
        regIntent.setPackage(GSF_PACKAGE);
        regIntent.putExtra(EXTRA_APPLICATION_PENDING_INTENT, PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        context.startService(regIntent);
        Log.i(TAG, "C2DM unregister service started");
    }

    private CloudRegistration()
    {
    }

}
