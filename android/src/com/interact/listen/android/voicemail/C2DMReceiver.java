package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;
import com.interact.listen.android.voicemail.client.ServerRegistrationInfo;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

import java.io.IOException;

public class C2DMReceiver extends C2DMBaseReceiver
{
    private static final String C2DM_ACCOUNT_EXTRA = "account_name";
    private static final String C2DM_MESSAGE_EXTRA = "message";
    
    private static final String C2DM_MESSAGE_SYNC = "sync-voicemails";
    private static final String C2DM_CONFIG_SYNC  = "sync-config";

    public C2DMReceiver()
    {
        super("listen-c2dm");
    }

    @Override
    public void onError(Context context, String errorId)
    {
        Log.i(TAG, "onError() " + errorId);
        Toast.makeText(context, "Listen registration error: " + errorId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRegistrered(Context context, String registrationId) throws IOException
    {
        Log.i(TAG, "onRegistrered() " + registrationId);
        super.onRegistrered(context, registrationId);
        SyncSchedule.syncConfig(getApplicationContext(), null);
    }

    @Override
    public void onUnregistered(Context context)
    {
        Log.i(TAG, "onUnregistered()");
        // we signal to the server to clear after the call to unregister(), so should be good
        super.onUnregistered(context);
    }
    
    @Override
    protected void onMessage(Context context, Intent intent)
    {
        Log.i(TAG, "onMessage()");
        String accountName = intent.getExtras().getString(C2DM_ACCOUNT_EXTRA);
        String message = intent.getExtras().getString(C2DM_MESSAGE_EXTRA);
        if(C2DM_MESSAGE_SYNC.equals(message))
        {
            if(accountName != null)
            {
                Log.i(TAG, "Messaging request received for account " + accountName);
                SyncSchedule.syncRegular(context, accountName, false);
            }
        }
        else if(C2DM_CONFIG_SYNC.equals(message))
        {
            if(accountName != null)
            {
                Log.i(TAG, "config sync request received for account: " + accountName);
                SyncSchedule.syncConfig(context, accountName);
            }
        }
    }

    public static void forceUnregister(Context context)
    {
        C2DMessaging.unregister(context);
        C2DMessaging.clearAllMetaData(context);
    }
    
    /**
     * Takes care of the registration state and registering/unregistering with Google.
     * 
     * @param context
     * @param info
     * @return null for no action needed, otherwise the registrationId to register with Listen ("" for unregister)
     */
    public static String refreshAppC2DMRegistrationState(Context context, ServerRegistrationInfo info, boolean accountSync, boolean force)
    {
        if(info == null)
        {
            Log.i(TAG, "server registration information not set, taking no action");
            return null;
        }

        final boolean autoSyncDesired = isAutoSyncDesired(context);
        final boolean updatedEnabled = C2DMessaging.setEnabled(context, info.isEnabled());
        
        String regState = null;
        
        if(autoSyncDesired && !accountSync)
        {
            // overall want to be registered, but this particular account does not (let account that wants to sync register)
            return info.getRegistrationId().length() == 0 ? null : "";
        }
        // Otherwise !autoSyncDesired and accountSync must be false -> we unregister overall
        // Or accountSync is true -> we register overall
        
        if(C2DMessaging.isSenderChange(context, info.getSenderId()))
        {
            if(C2DMessaging.setSenderId(context, info.getSenderId(), autoSyncDesired) && info.getRegistrationId().length() > 0)
            {
                regState = "";
            }
        }
        else
        {
            final String registrationId = C2DMessaging.getRegistrationId(context);
            final boolean autoSyncEnabled = registrationId.length() > 0;
    
            if(force || autoSyncEnabled != autoSyncDesired)
            {
                // possible we are in the middle of a register, but shouldn't cause any problems
                
                Log.i(TAG, "System-wide desirability for Listen Voicemail auto sync has changed; " +
                           (autoSyncDesired ? "registering" : "unregistering") + " application with C2DM servers.");
    
                if(autoSyncDesired)
                {
                    C2DMessaging.register(context);
                }
                else
                {
                    if(autoSyncEnabled)
                    {
                        // clear registration id if it doesn't look like we already did (i.e., don't force another one)
                        C2DMessaging.unregister(context);
                    }
                    if(info.getRegistrationId().length() > 0)
                    {
                        // looks like we need to clear the registration on the server as well
                        regState = "";
                    }
                }
            }
            else
            {
                regState = info.getRegistrationId().equals(registrationId) ? null : registrationId;
            }
        }
        
        if(updatedEnabled)
        {
            SyncSchedule.updatePeriodicSync(context);
        }
        
        return regState;
    }
    
    private static boolean isAutoSyncDesired(Context context)
    {
        boolean autoSyncDesired = false;
        if(ContentResolver.getMasterSyncAutomatically())
        {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account account : accounts)
            {
                if(ContentResolver.getIsSyncable(account, VoicemailProvider.AUTHORITY) > 0 &&
                    ContentResolver.getSyncAutomatically(account, VoicemailProvider.AUTHORITY))
                {
                    autoSyncDesired = true;
                    break;
                }
            }
        }
        return autoSyncDesired;
    }
}
