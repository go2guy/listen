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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;

/**
 * Base class for C2D message receiver. Includes constants for the strings used in the protocol.
 */
public abstract class C2DMBaseReceiver extends IntentService
{
    protected static final String TAG = "C2DM";

    public static final String REGISTRATION_CALLBACK_INTENT = "com.google.android.c2dm.intent.REGISTRATION";
    
    private static final String C2DM_RETRY = "com.google.android.c2dm.intent.RETRY";
    private static final String C2DM_INTENT = "com.google.android.c2dm.intent.RECEIVE";

    public static final String EXTRA_UNREGISTERED = "unregistered";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_REGISTRATION_ID = "registration_id";

    public static final String ERR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
    public static final String ERR_ACCOUNT_MISSING = "ACCOUNT_MISSING";
    public static final String ERR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String ERR_TOO_MANY_REGISTRATIONS = "TOO_MANY_REGISTRATIONS";
    public static final String ERR_INVALID_PARAMETERS = "INVALID_PARAMETERS";
    public static final String ERR_INVALID_SENDER = "INVALID_SENDER";
    public static final String ERR_PHONE_REGISTRATION_ERROR = "PHONE_REGISTRATION_ERROR";

    private static final String WAKELOCK_KEY = "C2DM_LIB";

    private static PowerManager.WakeLock mWakeLock;

    /**
     * The C2DMReceiver class must create a no-arg constructor
     */
    public C2DMBaseReceiver(String name)
    {
        super(name);
    }

    /**
     * Called when a cloud message has been received.
     */
    protected abstract void onMessage(Context context, Intent intent);

    /**
     * Called on registration error. Override to provide better error messages. This is called in the context of a
     * Service - no dialog or UI.  Can also schedule retry by callilng scheduleRetry() if applicable.
     */
    protected abstract void onError(Context context, String errorId, boolean retryAllowed);

    /**
     * Called when a registration token has been received.
     */
    protected abstract void onRegistrered(Context context, String registrationId) throws IOException;

    /**
     * Called when the device has been unregistered.
     */
    protected abstract void onUnregistered(Context context);
    
    /**
     * Called when a retry is requested.
     */
    protected abstract void onRetry(Context context);
    
    @Override
    public final void onHandleIntent(Intent intent)
    {
        try
        {
            Context context = getApplicationContext();
            if(intent.getAction().equals(REGISTRATION_CALLBACK_INTENT))
            {
                handleRegistration(context, intent);
            }
            else if(intent.getAction().equals(C2DM_INTENT))
            {
                onMessage(context, intent);
            }
            else if(intent.getAction().equals(C2DM_RETRY))
            {
                onRetry(context);
            }
        }
        finally
        {
            // Release the power lock, so phone can get back to sleep.
            // The lock is reference counted by default, so multiple
            // messages are ok.

            // If the onMessage() needs to spawn a thread or do something else,
            // it should use it's own lock.
            mWakeLock.release();
        }
    }

    /**
     * Called from the broadcast receiver. Will process the received intent, call handleMessage(), registered(), etc. in
     * background threads, with a wake lock, while keeping the service alive.
     */
    static void runIntentInService(Context context, Intent intent)
    {
        if(mWakeLock == null)
        {
            // This is called from BroadcastReceiver, there is no init.
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
        }
        mWakeLock.acquire();

        String receiver = context.getPackageName() + ".sync.C2DMReceiver";
        intent.setClassName(context, receiver);

        context.startService(intent);

    }

    public static void cancelRetries(Context context)
    {
        PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0, new Intent(C2DM_RETRY), 0);

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(retryPIntent);
    }
    
    void scheduleRetry(final Context context, long backoffTimeMs)
    {
        Log.d(TAG, "Scheduling registration retry, backoff = " + backoffTimeMs);
        Intent retryIntent = new Intent(C2DM_RETRY);
        PendingIntent retryPIntent = PendingIntent.getBroadcast(context, 0 /* requestCode */, retryIntent, 0 /* flags */);

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME, backoffTimeMs, retryPIntent);
    }
    
    private void handleRegistration(final Context context, Intent intent)
    {
        final String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);
        String error = intent.getStringExtra(EXTRA_ERROR);
        String removed = intent.getStringExtra(EXTRA_UNREGISTERED);

        Log.d(TAG, "control: registrationId = " + registrationId + ", error = " + error + ", removed = " + removed);

        if(removed != null)
        {
            onUnregistered(context);
        }
        else if(error != null)
        {
            Log.e(TAG, "Registration error " + error);
            onError(context, error, ERR_SERVICE_NOT_AVAILABLE.equals(error));
        }
        else
        {
            try
            {
                onRegistrered(context, registrationId);
            }
            catch(IOException ex)
            {
                Log.e(TAG, "Registration error " + ex.getMessage());
            }
        }
    }
}
