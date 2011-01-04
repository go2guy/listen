package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Constants;

public final class SyncSchedule
{

    // Android will sync on it's own on changes to VoicemailProvider using it's SYNC_EXTRAS_UPLOAD
    // and syncadapter.xml android:supportsUploading.
    // We want to only upload on delete and change in isNew, so take care of it ourselves.
    
    private static final String SYNC_EXTRAS_UPLOAD   = "com.interact.listen.upload_sync";
    private static final String SYNC_EXTRAS_PERIODIC = "com.interact.listen.periodic_sync";
    private static final String SYNC_EXTRAS_CONFIG   = "com.interact.listen.config_sync";
    private static final String SYNC_EXTRAS_CLOUD    = "com.interact.listen.cloud_sync";
    private static final String SYNC_EXTRAS_ID       = "com.interact.listen.sync_id";
    
    private static final String TAG = Constants.TAG + "SyncSchedule";
    
    public static void accountAdded(Context context, Account account)
    {
        final String[] auths = Authority.getAuthorities();
        for(String auth : auths)
        {
            ContentResolver.setIsSyncable(account, auth, 1);
            ContentResolver.setSyncAutomatically(account, auth, true);
        }
    }
    
    public static void syncUser(Context context, String userName, Authority auth)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putLong(SYNC_EXTRAS_ID, SyncId.INSTANCE.getAndIncrement(context));
        requestSync(context, userName, bundle, SyncType.USER_SYNC, auth);
    }
    
    public static void syncCloud(Context context, String userName, Authority auth)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_CLOUD, true);
        requestSync(context, userName, bundle, SyncType.CLOUD_SYNC, auth);
    }
    
    public static void syncConfig(Context context, String userName)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_CONFIG, true);
        bundle.putLong(SYNC_EXTRAS_ID, SyncId.INSTANCE.getAndIncrement(context));
        requestSync(context, userName, bundle, SyncType.CONFIG_SYNC, null);
    }

    public static void syncUpdates(Context context, String userName, Authority auth)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_UPLOAD, true);
        //bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);

        requestSync(context, userName, bundle, SyncType.UPLOAD_ONLY, auth);
    }

    static void removeLegacySync(Account account, Bundle extras, String authority)
    {
        Log.v(Constants.TAG, "removing legacy sync for " + account.name + ": " + extras);
        removeSync(account, extras, authority);
    }

    static void setPeriodicSync(Context context, Account account, boolean enabled, Authority auth)
    {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_PERIODIC, true);

        if(account != null)
        {
            if(enabled)
            {
                long interval = ApplicationSettings.getSyncIntervalMinutes(context) * 60L;
                Log.v(Constants.TAG, "adding periodic sync for " + account.name + " at interval " + interval);
                addSync(account, bundle, interval, auth);
            }
            else
            {
                Log.v(Constants.TAG, "removing periodic sync for " + account.name);
                removeSync(account, bundle, auth);
            }
        }
        else
        {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            if(enabled)
            {
                long interval = ApplicationSettings.getSyncIntervalMinutes(context) * 60L;
                for(Account acc : accounts)
                {
                    Log.v(Constants.TAG, "updating periodic sync for " + acc.name + " at interval " + interval);
                    addSync(acc, bundle, interval, auth);
                }
            }
            else
            {
                for(Account acc : accounts)
                {
                    Log.v(Constants.TAG, "removing periodic sync for " + acc.name);
                    removeSync(acc, bundle, auth);
                }
            }
        }
    }
    
    static SyncType getSyncType(Bundle extras)
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
    
    static long getSyncID(Bundle extras)
    {
        return extras == null ? 0 : extras.getLong(SYNC_EXTRAS_ID);
    }
    
    static void clearMeta(Context context)
    {
        SyncId.INSTANCE.clearMeta(context);
    }
    
    private static void addSync(Account account, Bundle bundle, long interval, Authority authority)
    {
        if(authority != null)
        {
            ContentResolver.addPeriodicSync(account, authority.get(), bundle, interval);
        }
        else
        {
            final String[] auths = Authority.getAuthorities();
            for(String auth : auths)
            {
                ContentResolver.addPeriodicSync(account, auth, bundle, interval);
            }
        }
    }
    
    private static void removeSync(Account account, Bundle bundle, Authority authority)
    {
        if(authority != null)
        {
            removeSync(account, bundle, authority.get());
        }
        else
        {
            final String[] auths = Authority.getAuthorities();
            for(String auth : auths)
            {
                removeSync(account, bundle, auth);
            }
        }
    }

    private static void removeSync(Account account, Bundle bundle, String authority)
    {
        ContentResolver.removePeriodicSync(account, authority, bundle);
    }
    
    private static void requestSync(Context context, String userName, Bundle bundle, SyncType type, Authority authority)
    {
        final String[] auths = authority == null ? Authority.getAuthorities() : new String[]{authority.get()};
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            if(userName == null || userName.equals(account.name))
            {
                Log.v(Constants.TAG, "requesting " + type.name() + " for: " + account.name);
                for(String auth : auths)
                {
                    ContentResolver.requestSync(account, auth, bundle);
                }
            }
        }
    }

    private SyncSchedule()
    {
    }
    
    private enum SyncId
    {
        INSTANCE;
        
        public synchronized long getAndIncrement(Context context)
        {
            final SharedPreferences sp = context.getSharedPreferences("sync_schedule", Context.MODE_PRIVATE);
            long id = sp.getLong("sync_id", 1);
            sp.edit().putLong("sync_id", id + 1).commit();
            return id;
        }
        
        public synchronized void clearMeta(Context context)
        {
            final SharedPreferences sp = context.getSharedPreferences("sync_schedule", Context.MODE_PRIVATE);
            sp.edit().clear().commit();
        }
    }
}
