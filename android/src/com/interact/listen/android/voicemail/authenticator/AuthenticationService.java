package com.interact.listen.android.voicemail.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;

public class AuthenticationService extends Service
{
    private static final String TAG = Constants.TAG + "AuthService";
    
    private Authenticator mAuthenticator;

    @Override
    public void onCreate()
    {
        Log.v(TAG, "Authentication Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, "Authentication Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        if (Log.isLoggable(TAG, Log.VERBOSE))
        {
            Log.v(TAG, "AccountAuthenticator for intent: " + intent);
        }
        return mAuthenticator.getIBinder();
    }
}
