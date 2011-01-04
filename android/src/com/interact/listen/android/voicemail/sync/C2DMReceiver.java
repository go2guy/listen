package com.interact.listen.android.voicemail.sync;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.interact.listen.android.voicemail.Constants;

import java.io.IOException;

public class C2DMReceiver extends C2DMBaseReceiver
{
    private static final String C2DM_ACCOUNT_EXTRA = "account_name";
    private static final String C2DM_MESSAGE_EXTRA = "message";
    
    private static final String C2DM_MESSAGE_SYNC  = "sync-voicemails";
    private static final String C2DM_CONTACTS_SYNC = "sync-contacts";
    private static final String C2DM_CONFIG_SYNC   = "sync-config";

    public C2DMReceiver()
    {
        super("listen-c2dm");
    }

    @Override
    public void onError(Context context, String errorId, boolean retryAllowed)
    {
        Log.i(TAG, "onError() " + errorId);
        Toast.makeText(context, "Listen registration error: " + errorId, Toast.LENGTH_LONG).show();
        CloudState.INSTANCE.reportErrored(context);
    }

    @Override
    public void onRegistrered(Context context, String registrationId) throws IOException
    {
        Log.i(TAG, "onRegistrered() " + registrationId);
        CloudState.INSTANCE.reportRegistered(context, registrationId);
        SyncSchedule.syncConfig(getApplicationContext(), null);
    }

    @Override
    public void onUnregistered(Context context)
    {
        Log.i(TAG, "onUnregistered()");
        CloudState.INSTANCE.reportUnregistered(context);
    }

    @Override
    protected void onRetry(Context context)
    {
        CloudRegistration.register(context, CloudState.INSTANCE.getSenderId(context));
    }

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        Log.i(TAG, "onMessage()");
        String accountName = intent.getExtras().getString(C2DM_ACCOUNT_EXTRA);
        String message = intent.getExtras().getString(C2DM_MESSAGE_EXTRA);
        
        if(AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE).length == 0)
        {
            // we don't have any accounts but we are getting messages, unregister
            Log.e(TAG, "received C2DM message but we don't have any accounts, unregistering...");
            CloudRegistration.unregister(context);
            return;
        }

        if(C2DM_MESSAGE_SYNC.equals(message))
        {
            Log.i(TAG, "Messaging request received for account " + accountName);
            SyncSchedule.syncCloud(context, accountName, Authority.VOICEMAIL);
        }
        else if(C2DM_CONTACTS_SYNC.equals(message))
        {
            Log.i(TAG, "contacts sync request received: " + accountName);
            SyncSchedule.syncCloud(context, accountName, Authority.CONTACTS);
        }
        else if(C2DM_CONFIG_SYNC.equals(message))
        {
            Log.i(TAG, "config sync request received for account: " + accountName);
            SyncSchedule.syncConfig(context, accountName);
        }
    }

}
