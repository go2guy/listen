package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;

public final class SyncSchedule
{

    // Android will sync on it's own on changes to VoicemailProvider using it's SYNC_EXTRAS_UPLOAD
    // and syncadapter.xml android:supportsUploading.
    // We want to only upload on delete and change in isNew, so take care of it ourselves.
    
    private static final String SYNC_EXTRAS_UPLOAD = "com.interact.listen.upload_sync";
    private static final String SYNC_EXTRAS_PERIODIC = "com.interact.listen.periodic_sync";
    private static final String SYNC_EXTRAS_CONFIG = "com.interact.listen.config_sync";
    private static final String SYNC_EXTRAS_CLOUD = "com.interact.listen.cloud_sync";

    private static final String TAG = Constants.TAG + "SyncSchedule";
    
    public static void accountAdded(Context context, Account account)
    {
        ContentResolver.setIsSyncable(account, VoicemailProvider.AUTHORITY, 1);
        ContentResolver.setSyncAutomatically(account, VoicemailProvider.AUTHORITY, true);
    }

    static void setPeriodicSync(Context context, Account account, boolean enabled)
    {
        long interval = ApplicationSettings.getSyncIntervalMinutes(context) * 60;

        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_PERIODIC, true);

        if(account != null)
        {
            if(enabled)
            {
                Log.v(Constants.TAG, "adding periodic sync for " + account.name + " at interval " + interval);
                ContentResolver.addPeriodicSync(account, VoicemailProvider.AUTHORITY, bundle, interval);
            }
            else
            {
                Log.v(Constants.TAG, "removing periodic sync for " + account.name);
                ContentResolver.removePeriodicSync(account, VoicemailProvider.AUTHORITY, bundle);
            }
        }
        else
        {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            if(enabled)
            {
                for(Account acc : accounts)
                {
                    Log.v(Constants.TAG, "updating periodic sync for " + acc.name + " at interval " + interval);
                    ContentResolver.addPeriodicSync(acc, VoicemailProvider.AUTHORITY, bundle, interval);
                }
            }
            else
            {
                for(Account acc : accounts)
                {
                    Log.v(Constants.TAG, "removing periodic sync for " + acc.name);
                    ContentResolver.removePeriodicSync(acc, VoicemailProvider.AUTHORITY, bundle);
                }
            }
        }
    }
    
    static void removeLegacySync(Context context, Account account, Bundle extras)
    {
        Log.v(Constants.TAG, "removing legacy sync for " + account.name + ": " + extras);
        ContentResolver.removePeriodicSync(account, VoicemailProvider.AUTHORITY, extras);
    }
    
    public static void syncUpdates(Context context, String userName)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_UPLOAD, true);
        //bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);

        requestSync(context, userName, bundle, SyncType.UPLOAD_ONLY);
    }
    
    public static void syncRegular(Context context, String userName, boolean fromUser)
    {
        Bundle bundle = new Bundle();
        if(fromUser)
        {
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            requestSync(context, userName, bundle, SyncType.USER_SYNC);
        }
        else
        {
            bundle.putBoolean(SYNC_EXTRAS_CLOUD, true);
            requestSync(context, userName, bundle, SyncType.CLOUD_SYNC);
        }
    }
    
    public static void syncConfig(Context context, String userName)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_CONFIG, true);
        requestSync(context, userName, bundle, SyncType.CONFIG_SYNC);
    }
    
    private static void requestSync(Context context, String userName, Bundle bundle, SyncType type)
    {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            if(userName == null || userName.equals(account.name))
            {
                Log.v(Constants.TAG, "requesting " + type.name() + " for: " + account.name);
                ContentResolver.requestSync(account, VoicemailProvider.AUTHORITY, bundle);
            }
        }
    }

    public static SyncType getSyncType(Bundle extras)
    {
        if(extras == null)
        {
            Log.e(TAG, "sync extras is null");
            return SyncType.INITIALIZE;
        }
        if(extras.containsKey("sync_type"))
        {
            return SyncType.LEGACY;
        }
        if(extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false))
        {
            return SyncType.INITIALIZE;
        }
        if(extras.getBoolean(SYNC_EXTRAS_UPLOAD, false))
        {
            return SyncType.UPLOAD_ONLY;
        }
        if(extras.getBoolean(SYNC_EXTRAS_PERIODIC, false))
        {
            return SyncType.PERIODIC;
        }
        if(extras.getBoolean(SYNC_EXTRAS_CONFIG, false))
        {
            return SyncType.CONFIG_SYNC;
        }
        if(extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false))
        {
            return SyncType.USER_SYNC;
        }
        if(extras.getBoolean(SYNC_EXTRAS_CLOUD, false))
        {
            return SyncType.CLOUD_SYNC;
        }
        Log.i(TAG, "sync type unknown " + extras);
        return SyncType.INITIALIZE;
    }
    
    private SyncSchedule()
    {
    }
}
