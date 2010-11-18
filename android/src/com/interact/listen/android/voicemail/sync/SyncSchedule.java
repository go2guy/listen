package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;

public final class SyncSchedule
{
    public static final String PREFERENCES_INT_SYNC_INTERVAL_MINUTES = "sync_interval_minutes";

    public static final String EXTRA_INT_SYNC_TYPE = "sync_type";

    public static final int SYNC_TYPE_SEND_UPDATES = 0x01;
    public static final int SYNC_TYPE_NEW          = 0x03;
    public static final int SYNC_TYPE_FULL         = 0x07;

    private static final int DEFUALT_SYNC_MINUTES = 5;

    private static Object syncObject = new Object();
    private static long lastFullSyncMS = 0;
    
    public static void addPeriodicSync(Context context, Account account)
    {
        ContentResolver.setIsSyncable(account, VoicemailProvider.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, VoicemailProvider.AUTHORITY, true);

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_INT_SYNC_TYPE, SYNC_TYPE_NEW);
        
        long interval = getSyncIntervalMinutes(context) * 60;
        ContentResolver.addPeriodicSync(account, VoicemailProvider.AUTHORITY, new Bundle(), interval);
    }
    
    public static void updatePeriodicSync(Context context)
    {
        long interval = getSyncIntervalMinutes(context) * 60;

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_INT_SYNC_TYPE, SYNC_TYPE_NEW);
        
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            Log.v(Constants.TAG, "adding periodic sync for " + account.name + " at interval " + interval);
            ContentResolver.addPeriodicSync(account, VoicemailProvider.AUTHORITY, bundle, interval);
        }
    }

    public static void syncUpdate(Context context, String userName)
    {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_INT_SYNC_TYPE, SYNC_TYPE_SEND_UPDATES);
        
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            if(userName == null || userName.equals(account.name))
            {
                Log.v(Constants.TAG, "requesting update sync for " + account.name);
                ContentResolver.requestSync(account, VoicemailProvider.AUTHORITY, bundle);
            }
        }
    }
    
    public static void syncFull(Context context, boolean force)
    {
        long nowMS = System.currentTimeMillis();
        long intervalMS = getSyncIntervalMinutes(context) * 60000L;

        synchronized(syncObject)
        {
            if(!force && lastFullSyncMS + intervalMS > nowMS)
            {
                return; // only request a full sync as often as the periodic check for new voicemails
            }
            lastFullSyncMS = nowMS;
        }
        
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_INT_SYNC_TYPE, SYNC_TYPE_FULL);
        
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            Log.v(Constants.TAG, "requesting full sync for " + account.name);
            ContentResolver.requestSync(account, VoicemailProvider.AUTHORITY, bundle);
        }
    }
    
    public static int getSyncType(Bundle extras)
    {
        if(extras == null || !extras.containsKey(EXTRA_INT_SYNC_TYPE))
        {
            Log.v(Constants.TAG, "no sync type provided, doing full sync");
            return SYNC_TYPE_FULL;
        }
        return extras.getInt(EXTRA_INT_SYNC_TYPE);
    }
    
    public static int getSyncIntervalMinutes(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREFERENCES_INT_SYNC_INTERVAL_MINUTES, DEFUALT_SYNC_MINUTES);
    }
    
    private SyncSchedule()
    {
    }
}
