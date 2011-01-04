package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.client.AccountInfo;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.client.ServerRegistrationInfo;
import com.interact.listen.android.voicemail.sync.CloudState.UpdateState;

import java.io.IOException;

import org.apache.http.ParseException;

public abstract class AbstractCloudSyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = Constants.TAG + "CloudAdapter";

    private static final String PROP_LAST_SYNC_ID  = "last_sync_id";
    private static final String CLOUD_PREFS = "cloud_adapter";
    
    private final AccountManager mAccountManager;
    
    public AbstractCloudSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }
    
    static boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }
    
    AccountManager getAccountManager()
    {
        return mAccountManager;
    }
    
    abstract void authoritySync(AccountInfo aInfo, SyncType syncType, Authority authority,
                                ContentProviderClient provider, SyncResult syncResult,
                                SharedPreferences prefs)
        throws AuthorizationException, RemoteException, IOException;

    public static void updatePeriodicSync(Context context, Authority authority)
    {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            if(authority == null)
            {
                for(Authority auth : Authority.values())
                {
                    final boolean isActive = CloudState.INSTANCE.isCloudSyncActive(context, account, auth);
                    SyncSchedule.setPeriodicSync(context, account, isActive, auth);
                }
            }
            else
            {
                final boolean isActive = CloudState.INSTANCE.isCloudSyncActive(context, account, authority);
                SyncSchedule.setPeriodicSync(context, account, isActive, authority);
            }
        }
    }

    public static void removeAccountInfo(Context context, Account account) throws IOException, OperationCanceledException
    {
        AccountManager am = AccountManager.get(context);
        
        boolean clearRegistration = false;
        
        if(account != null)
        {
            unregisterClient(context, account, null);
            CloudState.INSTANCE.clearMeta(context, account);
            clearAuthorityMeta(context, account);
            clearRegistration = am.getAccountsByType(Constants.ACCOUNT_TYPE).length <= 1;
        }
        else
        {
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account acc : accounts)
            {
                try
                {
                    unregisterClient(context, acc, null);
                    CloudState.INSTANCE.clearMeta(context, acc);
                    clearAuthorityMeta(context, acc);
                }
                catch(Exception e)
                {
                    Log.e(TAG, "exception cleaning out account " + acc.name, e);
                }
            }
            clearRegistration = true;
        }
        
        if(clearRegistration)
        {
            final SharedPreferences regMeta = context.getSharedPreferences(CLOUD_PREFS, Context.MODE_PRIVATE);
            regMeta.edit().clear().commit();
            SyncSchedule.clearMeta(context);

            CloudRegistration.unregister(context);
            CloudState.INSTANCE.clearMeta(context);
        }
    }

    @Override
    public final void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                                    SyncResult syncResult)
    {
        SyncType syncType = SyncSchedule.getSyncType(extras);
        Log.i(TAG, "on perform sync " + account.name + ": " + syncType + " authority: " + authority);

        Authority auth = Authority.getByAuthority(authority);
        if(auth == null)
        {
            Log.e(TAG, "unknown authority: " + authority);
            SyncSchedule.removeLegacySync(account, extras, authority);
            return;
        }
        
        if(syncType == SyncType.LEGACY)
        {
            Log.w(TAG, "legacy sync encountered, removing legacy sync and performing INITIALIZE sync");
            SyncSchedule.removeLegacySync(account, extras, authority);
            syncType = SyncType.INITIALIZE;
        }

        if(syncType == SyncType.INITIALIZE)
        {
            if(ContentResolver.getIsSyncable(account, authority) <= 0)
            {
                // ensure we are set to be syncable
                ContentResolver.setIsSyncable(account, authority, 1);
            }
        }

        AccountInfo aInfo = getAccountInfo(syncType, account, syncResult);
        if(aInfo == null)
        {
            checkAndSetPeriodic(getContext(), account, auth);
            return;
        }
        
        final SharedPreferences regMeta = getContext().getSharedPreferences(CLOUD_PREFS, Context.MODE_PRIVATE);

        final TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = tm.getDeviceId();

        synchronized (CloudState.INSTANCE)
        {
            try
            {
                final long lastSyncID = regMeta.getLong(PROP_LAST_SYNC_ID, 0);
                final long newSyncID = SyncSchedule.getSyncID(extras);
                
                boolean fRef = ((syncType == SyncType.USER_SYNC || syncType == SyncType.CONFIG_SYNC) &&
                                (newSyncID == 0 || newSyncID != lastSyncID)) || syncType == SyncType.INITIALIZE;
                
                ServerRegistrationInfo info = null;
                
                if(fRef || CloudState.INSTANCE.isServerRegistrationCheckNeeded(getContext(), account))
                {
                    try
                    {
                        info = ClientUtilities.getServerRegistrationInfo(aInfo, deviceId);
                    }
                    catch(AuthorizationException e)
                    {
                        Log.e(TAG, "AuthorizationException", e);
                        mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, aInfo.getAuthToken());
                        syncResult.stats.numAuthExceptions++;
                        return;
                    }
                    
                    if(isInterrupted())
                    {
                        Log.i(TAG, "sync interrupted after getting C2DM registration information");
                        return;
                    }
                }
                if(info != null)
                {
                    UpdateState state = CloudState.INSTANCE.updateServerRegistration(getContext(), info, account);
                    if(state.isServerRegistrationNeeded())
                    {
                        try
                        {
                            if(ClientUtilities.registerDevice(deviceId, aInfo, state.getRegistrationID(),
                                                              state.getEnableAuthorities(),
                                                              state.getDisableAuthorities()))
                            {
                                CloudState.INSTANCE.commitUpdateState(state, getContext(), account);
                                Log.i(TAG, "updated account registration state: '" + state.getRegistrationID() + "'");
                            }
                            else
                            {
                                Log.e(TAG, "failed account registration update: '" + state.getRegistrationID() + "'");
                            }
                        }
                        catch(IOException e)
                        {
                            Log.e(TAG, "IOException", e);
                            syncResult.stats.numIoExceptions++;
                            return;
                        }
                        catch(AuthorizationException e)
                        {
                            Log.e(TAG, "AuthorizationException", e);
                            mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, aInfo.getAuthToken());
                            syncResult.stats.numAuthExceptions++;
                            return;
                        }
                    }
                }
    
                if(newSyncID > 0)
                {
                    regMeta.edit().putLong(PROP_LAST_SYNC_ID, newSyncID).commit();
                }
            }
            finally
            {
                checkAndSetPeriodic(getContext(), account, auth);
            }
        }

        if(isInterrupted())
        {
            Log.i(TAG, "sync interrupted after cloud configuration update");
            return;
        }

        try
        {
            final SharedPreferences ap = getAuthorityPrefs(getContext(), account, auth.get());
            authoritySync(aInfo, syncType, auth, provider, syncResult, ap);
        }
        catch(final AuthorizationException e)
        {
            getAccountManager().invalidateAuthToken(Constants.ACCOUNT_TYPE, aInfo.getAuthToken());
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthorizationException", e);
        }
        catch(final RemoteException e)
        {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "RemoteException", e);
        }
        catch(final ParseException e)
        {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
        catch(final IOException e)
        {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "IOException", e);
        }
    }

    private static void clearAuthorityMeta(Context context, Account account)
    {
        final String[] auths = Authority.getAuthorities();
        for(String auth : auths)
        {
            getAuthorityPrefs(context, account, auth).edit().clear().commit();
        }
    }
    
    private static SharedPreferences getAuthorityPrefs(Context context, Account account, String auth)
    {
        return context.getSharedPreferences(CLOUD_PREFS + ":" + account.name + ":" + auth, Context.MODE_PRIVATE);
    }
    
    private static void checkAndSetPeriodic(Context context, Account account, Authority auth)
    {
        boolean enabled = !CloudState.INSTANCE.isCloudSyncActive(context, account, auth);
        Log.i(TAG, "setting periodic sync for " + account.name + ":" + auth.get() + " enabled: " + enabled);
        SyncSchedule.setPeriodicSync(context, account, enabled, auth);
    }
    
    private AccountInfo getAccountInfo(SyncType syncType, Account account, SyncResult syncResult)
    {
        AccountInfo aInfo = null;
        try
        {
            aInfo = AccountInfo.getAccountInfo(mAccountManager, account);

            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted after authorization");
                aInfo = null;
            }
        }
        catch(final AuthenticatorException e)
        {
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthenticatorException", e);
        }
        catch(final OperationCanceledException e)
        {
            Log.e(TAG, "OperationCanceledExcetpion", e);
        }
        catch(final IOException e)
        {
            Log.e(TAG, "IOException getting account information", e);
            syncResult.stats.numIoExceptions++;
        }
        return aInfo;
    }
    
    private static void unregisterClient(Context context, Account account, Authority authority)
        throws IOException, OperationCanceledException
    {
        Log.i(TAG, "unregistring client on server: " + account.name + " auth: " + authority);

        AccountManager manager = AccountManager.get(context);
        try
        {
            AccountInfo aInfo = AccountInfo.getAccountInfo(manager, account);
            if(aInfo == null)
            {
                return;
            }
            
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String clientDeviceId = tm.getDeviceId();
    
            ClientUtilities.registerDevice(clientDeviceId, aInfo, "", null, null);
        }
        catch(AuthorizationException e)
        {
            Log.e(TAG, "authorization exception", e);
        }
        catch(AuthenticatorException e)
        {
            Log.e(TAG, "authenticator exception", e);
        }
    }

}
