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
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.C2DMReceiver;
import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.DownloadRunnable;
import com.interact.listen.android.voicemail.NotificationHelper;
import com.interact.listen.android.voicemail.Voicemail;
import com.interact.listen.android.voicemail.VoicemailNotifyReceiver;
import com.interact.listen.android.voicemail.authenticator.Authenticator;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.client.ServerRegistrationInfo;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = Constants.TAG + "SyncAdapter";
    private static final long CLEAN_INTERVAL = 24 * 3600000; // once a day

    public static final String LAST_CLEAN = "last_audio_clean";
    public static final String SERVER_LAST_SYNC = "server_last_sync";
    
    static final String REGISTERED_SYNC = "sync_registered";

    private final AccountManager mAccountManager;
    
    public SyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }
    
    private static boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }
    
    private static int insertFromList(ContentProviderClient provider, List<Voicemail> insertList) throws RemoteException
    {
        int inserts = VoicemailHelper.insertVoicemails(provider, insertList);
        if(inserts != insertList.size())
        {
            Log.e(TAG, "inserted " + inserts + " when expecting " + insertList.size());
        }
        insertList.clear();
        return inserts;
    }
    
    private List<Voicemail> insertNewVoicemails(Account account, ContentProviderClient provider, SyncResult syncResult,
                                               SyncIter iter, boolean fullList) throws RemoteException
    {
        boolean notifyEnabled = ApplicationSettings.isNotificationEnabled(getContext());
        
        List<Voicemail> needAudio = new ArrayList<Voicemail>();
        List<Voicemail> insertList = new ArrayList<Voicemail>();

        int notifications = 0;
        int notifyId = 0;
        
        boolean keepNotification = false;
        
        iter.reset();
        while(iter.next())
        {
            if(iter.isMissingFromLocal())
            {
                Voicemail v = iter.getServer();
                if(!notifyEnabled)
                {
                    v.setNotified(true);
                }
                Log.i(TAG, "voicemail added on server: " + v.getVoicemailId());
                insertList.add(v);

                if(VoicemailHelper.shouldAttemptDownload(v, false))
                {
                    needAudio.add(v);
                }
                if(v.needsNotification())
                {
                    if(++notifications == 1)
                    {
                        // lets get the id of one notification in case we need it for broadcast
                        VoicemailHelper.insertVoicemail(provider, v);
                        notifyId = v.getId();
                        insertList.remove(insertList.size() - 1);
                        Log.i(TAG, "first notification from insert: " + notifyId);
                    }
                }
                if(insertList.size() >= 50)
                {
                    syncResult.stats.numInserts += insertFromList(provider, insertList);
                }
            }
            else if(iter.isMatched())
            {
                if(iter.getServer().needsNotification() && iter.getLocal().needsNotification())
                {
                    if(++notifications == 1)
                    {
                        notifyId = iter.getLocal().getId();
                        Log.i(TAG, "first notification from local: " + notifyId);
                    }
                }
            }
            else if(!fullList && iter.getLocal().needsNotification())
            {
                // to be on the safe side (e.g., periodic update)
                keepNotification = true;
            }
        }

        syncResult.stats.numInserts += insertFromList(provider, insertList);
        
        if(notifications == 0 && !keepNotification)
        {
            NotificationHelper.clearVoicemailNotifications(getContext());
        }
        else if(notifications == 1 && notifyId > 0)
        {
            VoicemailNotifyReceiver.broadcastVoicemailNotification(getContext(), provider, account.name, notifyId);
        }
        else if(notifications > 0)
        {
            VoicemailNotifyReceiver.broadcastVoicemailNotifications(getContext(), provider, account.name, notifications);
        }
        
        return needAudio;
    }
    
    private void deleteMissingVoicemails(ContentProviderClient provider, SyncResult syncResult, SyncIter iter)
        throws RemoteException
    {
        iter.reset();
        List<Voicemail> deleteList = new ArrayList<Voicemail>(25);
        while(iter.next())
        {
            if(iter.isMissingFromServer())
            {
                Voicemail v = iter.getLocal();
                Log.i(TAG, "voicemail was deleted on server: " + v.getVoicemailId());
                deleteList.add(v);
            }
        }
        syncResult.stats.numDeletes += VoicemailHelper.deleteVoicemails(provider, deleteList);
    }
    
    private boolean pushVoicemailUpdates(Uri host, String authToken, Account account, ContentProviderClient provider,
                                         SyncResult syncResult, List<Voicemail> voicemails, String deviceId)
        throws IOException, AuthorizationException, RemoteException
    {
        for(Voicemail voicemail : voicemails)
        {
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted syncing new update " + voicemail.getVoicemailId());
                return false;
            }
            
            if(voicemail.isMarkedTrash())
            {
                Log.i(TAG, "pushing delete to server " + voicemail.getVoicemailId());
                if(ClientUtilities.deleteVoicemail(deviceId, host, voicemail.getVoicemailId(), account.name, authToken))
                {
                    Log.i(TAG, "removing local voicemail after sync " + voicemail.getVoicemailId());
                    VoicemailHelper.deleteVoicemail(provider, voicemail);
                    syncResult.stats.numDeletes++;
                }
            }
            else if(voicemail.isMarkedRead())
            {
                Log.i(TAG, "pushing read voicemail to server: " + voicemail.getVoicemailId());
                boolean succ = ClientUtilities.markVoicemailRead(deviceId, host, voicemail.getVoicemailId(), account.name,
                                                                 authToken, !voicemail.getIsNew());
                if(succ)
                {
                    Log.i(TAG, "clear marked read locally: " + voicemail.getId());
                    VoicemailHelper.clearMarkedRead(provider, voicemail);
                    syncResult.stats.numUpdates++;
                }
            }
        }
        return true;
    }

    
    private boolean updateLocalVoicemails(ContentProviderClient provider, SyncResult syncResult, SyncIter iter,
                                          List<Voicemail> needAudio) throws RemoteException
    {
        iter.reset();
        while(iter.nextMatch())
        {
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted while sending updates");
                return false;
            }

            if(VoicemailHelper.updateVoicemail(provider, iter.getLocal(), iter.getServer()))
            {
                syncResult.stats.numUpdates++;
            }
            if(VoicemailHelper.shouldAttemptDownload(iter.getLocal(), false))
            {
                needAudio.add(iter.getLocal());
            }
        }
        return true;
    }

    private void downloadAudio(Uri host, String authToken, Account account, ContentProviderClient provider,
                               SyncResult syncResult, List<Voicemail> voicemails)
    {
        if(voicemails == null || voicemails.isEmpty() || !ApplicationSettings.isSyncAudio(getContext()))
        {
            return;
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS,
                                                             new PriorityBlockingQueue<Runnable>(5));

        for(Voicemail vm : voicemails)
        {
            DownloadRunnable downloadTask = new DownloadRunnable(getContext(), vm);
            downloadTask.setAccount(account, authToken);
            downloadTask.setProvider(provider);
            executor.execute(downloadTask);
        }

        executor.shutdown();
        boolean killed = false;
        
        while(!executor.isTerminated())
        {
            if(isInterrupted() && !killed)
            {
                executor.shutdownNow();
                killed = true;
            }
            try
            {
                executor.awaitTermination(5, TimeUnit.SECONDS);
                if(executor.isTerminated())
                {
                    Log.v(TAG, "executors done downloading: " + voicemails.size());
                    break;
                }
            }
            catch(InterruptedException e)
            {
                continue;
            }
        }
        
    }
    
    static SharedPreferences getSyncMeta(Context context, Account account)
    {
        return context.getSharedPreferences("sync:" + account.name, 0);
    }
    
    private SharedPreferences getSyncMeta(Account account)
    {
        return getSyncMeta(getContext(), account);
    }
    
    private void cleanAudio(SharedPreferences syncMeta, ContentProviderClient provider, Account account) throws RemoteException
    {
        long lastAudioClean = syncMeta.getLong(LAST_CLEAN, 0);
        long now = System.currentTimeMillis();
        
        if(!isInterrupted() && now >= lastAudioClean + CLEAN_INTERVAL)
        {
            syncMeta.edit().putLong(LAST_CLEAN, now).commit();
            if(lastAudioClean > 0) // no reason to clean on first us
            {
                Log.i(TAG, "cleaning old voicemails [" + now + " >= " + lastAudioClean + "]");
                VoicemailHelper.deleteOldAudio(provider, account.name);
            }
        }
    }
    
    public static void updatePeriodicSync(Context context)
    {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            final boolean reg = getSyncMeta(context, account).getBoolean(REGISTERED_SYNC, false);
            if(!reg)
            {
                SyncSchedule.setPeriodicSync(context, account, !reg);
            }
        }
    }

    public static void removeAccountInfo(Context context, Account account) throws IOException, OperationCanceledException
    {
        AccountManager am = AccountManager.get(context);
        
        boolean clearRegistration = false;
        
        if(account != null)
        {
            unregisterClient(context, account, false);
            getSyncMeta(context, account).edit().clear().commit();
            clearRegistration = am.getAccountsByType(Constants.ACCOUNT_TYPE).length <= 1;
        }
        else
        {
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account acc : accounts)
            {
                try
                {
                    unregisterClient(context, acc, false);
                    getSyncMeta(context, acc).edit().clear().commit();
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
            C2DMReceiver.forceUnregister(context);
        }
    }

    private static void unregisterClient(Context context, Account account, boolean force) throws IOException, OperationCanceledException
    {
        final SharedPreferences syncMeta = getSyncMeta(context, account);
        final AccountManager accountManager = AccountManager.get(context);
        final boolean syncRegistered = syncMeta.getBoolean(REGISTERED_SYNC, false);

        if(!syncRegistered && !force)
        {
            Log.i(TAG, "client not registered on server: " + account.name);
            return;
        }
        
        Log.i(TAG, "unregistring client on server: " + account.name);

        try
        {
            String authToken = accountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
            if(authToken == null)
            {
                Log.e(TAG, "unable to authorize " + account.name);
                return;
            }
    
            String uId = accountManager.getUserData(account, Authenticator.ID_DATA);
            String hStr = accountManager.getUserData(account, Authenticator.HOST_DATA);
            if (hStr == null || uId == null)
            {
                Log.e(TAG, "authorization properties not set for " + account.name);
                return;
            }
            Log.i(TAG, "authorized " + hStr + " user ID " + uId);
    
            Uri host = Uri.parse(hStr);
    
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String clientDeviceId = tm.getDeviceId();
    
            ClientUtilities.registerDevice(host, account.name, authToken, "", clientDeviceId);
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
    
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult)
    {
        SyncType syncType = SyncSchedule.getSyncType(extras);
        Log.i(TAG, "on perform sync " + account.name + ": " + syncType);

        if(syncType == SyncType.LEGACY)
        {
            Log.w(TAG, "legacy sync encountered, removing periodic sync and performing INITIALIZE sync");
            SyncSchedule.removeLegacySync(getContext(), account, extras);
            syncType = SyncType.INITIALIZE;
        }
        
        final SharedPreferences syncMeta = getSyncMeta(account);
        
        long lastServerSyncTime = syncMeta.getLong(SERVER_LAST_SYNC, 0);

        final boolean syncDesired = ContentResolver.getMasterSyncAutomatically() &&
                                    ContentResolver.getSyncAutomatically(account, VoicemailProvider.AUTHORITY);
        final boolean serverReg = syncMeta.getBoolean(REGISTERED_SYNC, false);

        if(syncType == SyncType.INITIALIZE)
        {
            if(ContentResolver.getIsSyncable(account, VoicemailProvider.AUTHORITY) <= 0)
            {
                // ensure we are set to be syncable
                ContentResolver.setIsSyncable(account, VoicemailProvider.AUTHORITY, 1);
            }
            if(!serverReg)
            {
                // ensure we get a periodic sync going if needed
                SyncSchedule.setPeriodicSync(getContext(), account, true);
            }
        }
        
        String authToken = null;
        try
        {
            authToken = mAccountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
            if(authToken == null)
            {
                syncResult.stats.numAuthExceptions++;
                Log.e(TAG, "unable to authenticate");
                return;
            }

            String uId = mAccountManager.getUserData(account, Authenticator.ID_DATA);
            String hStr = mAccountManager.getUserData(account, Authenticator.HOST_DATA);
            if (hStr == null || uId == null)
            {
                syncResult.stats.numAuthExceptions++;
                Log.e(TAG, "seem to authenticate but no host and/or user ID?");
                return;
            }
            Log.i(TAG, "Authorized " + hStr + " user ID " + uId);

            Uri host = Uri.parse(hStr);
            long userID = Long.parseLong(uId);
            
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted after authorization");
                return;
            }

            TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String deviceId = tm.getDeviceId();

            final boolean forceRefresh = syncType == SyncType.INITIALIZE || syncType == SyncType.USER_SYNC;

            // could take out !serverReg, but would require manual refresh when going from
            // no sender ID to a sender ID to get cloud sync enabled
            final boolean checkInfo = forceRefresh || syncType == SyncType.CONFIG_SYNC || !serverReg;
            
            if(checkInfo)
            {
                ServerRegistrationInfo info = ClientUtilities.getServerRegistrationInfo(host, account.name, authToken, deviceId);
                
                if(isInterrupted())
                {
                    Log.i(TAG, "sync interrupted after getting C2DM registration information");
                    return;
                }
                
                final String reg = C2DMReceiver.refreshAppC2DMRegistrationState(getContext(), info, syncDesired, forceRefresh);
                if(reg != null)
                {
                    if(ClientUtilities.registerDevice(host, account.name, authToken, reg, deviceId))
                    {
                        syncMeta.edit().putBoolean(REGISTERED_SYNC, reg.length() > 0).commit();
                        SyncSchedule.setPeriodicSync(getContext(), account, false);
                        Log.i(TAG, "updated account registration state: '" + reg + "'");
                    }
                    else
                    {
                        syncMeta.edit().putBoolean(REGISTERED_SYNC, false).commit();
                        SyncSchedule.setPeriodicSync(getContext(), account, true);
                        Log.e(TAG, "failed account registration update: '" + reg + "'");
                    }
                }
            }

            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted after registration update");
                return;
            }

            if(syncType == SyncType.UPLOAD_ONLY)
            {
                Log.v(TAG, "doing update sync");
                List<Voicemail> localVoicemails = VoicemailHelper.getUpdatedVoicemails(provider, account.name);
                Log.i(TAG, "got updated voicemails for sync: " + localVoicemails);
                pushVoicemailUpdates(host, authToken, account, provider, syncResult, localVoicemails, deviceId);
                Log.i(TAG, "update push completed");
                return;
            }
            
            if(syncType == SyncType.PERIODIC) //&& !C2DMessaging.isEnabled(getContext()))
            {
                Log.i(TAG, "treating periodic sync as cloud sync");
                syncType = SyncType.CLOUD_SYNC;
            }

            Long[] syncTime = new Long[] {0L, lastServerSyncTime};
            if(syncType == SyncType.PERIODIC)
            {
                syncTime[0] = lastServerSyncTime;
            }
            
            List<Voicemail> serverVoicemails = ClientUtilities.retrieveVoicemails(host, userID, account.name, authToken, syncTime);
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted retrieving voicemails from server");
                return;
            }
            if(serverVoicemails == null)
            {
                Log.e(TAG, "got back null for list of server voicemails");
                syncResult.stats.numIoExceptions++;
                return;
            }
            Log.i(TAG, "Found voicemails on server: " + serverVoicemails.size());

            List<Voicemail> providerVoicemails = VoicemailHelper.getVoicemails(provider, account.name, 0, true);
            Log.i(TAG, "Got voicemails from provider: " + providerVoicemails.size());

            // each list ordered by the voicemail ID on the server
            
            SyncIter iter = new SyncIter(providerVoicemails, serverVoicemails);
            
            List<Voicemail> needAudio = insertNewVoicemails(account, provider, syncResult, iter, syncType != SyncType.PERIODIC);
            if(syncType != SyncType.PERIODIC)
            {
                deleteMissingVoicemails(provider, syncResult, iter);
            }
            pushVoicemailUpdates(host, authToken, account, provider, syncResult, providerVoicemails, deviceId);
            updateLocalVoicemails(provider, syncResult, iter, needAudio);
            
            syncMeta.edit().putLong(SERVER_LAST_SYNC, syncTime[1]).commit();

            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted");
                return;
            }
            
            downloadAudio(host, authToken, account, provider, syncResult, needAudio);
            cleanAudio(syncMeta, provider, account);

            Log.i(TAG, "synced voicemails");
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
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        }
        catch(final AuthorizationException e)
        {
            mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authToken);
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthorizationException", e);
        }
        catch(final ParseException e)
        {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
        catch(RemoteException e)
        {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "RemoteException", e);
        }
    }

}
