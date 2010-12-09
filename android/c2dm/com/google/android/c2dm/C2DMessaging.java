/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.c2dm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;

/**
 * Utilities for device registration. Will keep track of the registration token in a private preference.
 */
public final class C2DMessaging
{
    private static final String TAG = "C2DMessaging";
        
    private static final String EXTRA_SENDER = "sender";
    private static final String EXTRA_APPLICATION_PENDING_INTENT = "app";
    
    private static final String REQUEST_UNREGISTRATION_INTENT = "com.google.android.c2dm.intent.UNREGISTER";
    private static final String REQUEST_REGISTRATION_INTENT = "com.google.android.c2dm.intent.REGISTER";

    private static final String DM_REGISTRATION = "dm_registration";
    private static final String SENDER_ID = "sender_id";
    private static final String C2DMENABLED = "c2dm_enabled";
    private static final String BACKOFF = "backoff";
    
    private static final String GSF_PACKAGE = "com.google.android.gsf";

    private static final String PREFERENCE = "com.google.android.c2dm";

    private static final long DEFAULT_BACKOFF = 30000;
    
    /**
     * Handles updating the stored registration information and executing register or unregister if needed.
     * Only call if isSenderChange().
     * 
     * @param context
     * @param id new ID
     * @return true if an unregister was performed
     */
    public static boolean setSenderId(Context context, String id, boolean allowRegistration)
    {
        boolean needUnregister = false;
        
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);

        String oldId = prefs.getString(SENDER_ID, "");
        String regId = prefs.getString(DM_REGISTRATION, "");

        if(oldId.equals(id))
        {
            return false;
        }
        
        C2DMBaseReceiver.cancelRetries(context);

        if(!TextUtils.isEmpty(oldId) && TextUtils.isEmpty(id) && !TextUtils.isEmpty(regId))
        {
            // only unregister if we are registered and not going to be re-registering
            unregister(context);
            needUnregister = true;
        }

        Editor editor = prefs.edit();
        
        if(!TextUtils.isEmpty(regId))
        {
            editor.putString(DM_REGISTRATION, "");
        }
        
        editor.putString(SENDER_ID, id);
        editor.commit();

        if(!TextUtils.isEmpty(id) && allowRegistration)
        {
            register(context);
        }
        
        return needUnregister;
    }

    /**
     * Initiate c2d messaging registration for the current application
     */
    public static void register(Context context)
    {
        C2DMBaseReceiver.cancelRetries(context);

        String senderId = getSenderId(context);
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
            Log.e("Listen-C2DM", "C2DM service not found!");
        }
    }

    /**
     * Unregister the application. New messages will be blocked by server.
     */
    public static void unregister(Context context)
    {
        C2DMBaseReceiver.cancelRetries(context);
        clearRegistrationId(context);
        
        Intent regIntent = new Intent(REQUEST_UNREGISTRATION_INTENT);
        regIntent.setPackage(GSF_PACKAGE);
        regIntent.putExtra(EXTRA_APPLICATION_PENDING_INTENT, PendingIntent.getBroadcast(context, 0, new Intent(), 0));
        context.startService(regIntent);
    }

    public static void clearAllMetaData(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }
    
    public static boolean isSenderChange(Context context, String id)
    {
        return !getSenderId(context).equals(id);
    }
    
    public static boolean setEnabled(Context context, boolean enabled)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        boolean oldEnabled = prefs.getBoolean(C2DMENABLED, false);
        if(oldEnabled != enabled)
        {
            prefs.edit().putBoolean(C2DMENABLED, enabled).commit();
        }
        return oldEnabled != enabled;
    }
    
    public static boolean isEnabled(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return prefs.getBoolean(C2DMENABLED, false);
    }
    
    public static void registerEnabledListener(Context context, OnSharedPreferenceChangeListener listener)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterEnabledListener(Context context, OnSharedPreferenceChangeListener listener)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Return the current registration id. If result is empty, the registration has failed.
     * 
     * @return registration id, or empty string if the registration is not complete.
     */
    public static String getRegistrationId(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return prefs.getString(DM_REGISTRATION, "");
    }

    static long getBackoff(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return prefs.getLong(BACKOFF, DEFAULT_BACKOFF);
    }

    static void setBackoff(Context context, long backoff)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putLong(BACKOFF, backoff);
        editor.commit();
    }

    static void clearRegistrationId(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        prefs.edit().putString(DM_REGISTRATION, "").commit();
    }

    static void setRegistrationId(Context context, String registrationId)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(DM_REGISTRATION, registrationId);
        editor.commit();
    }
    
    private static String getSenderId(Context context)
    {
        final SharedPreferences prefs = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return prefs.getString(SENDER_ID, "");
    }
    
    private C2DMessaging()
    {
    }
}
