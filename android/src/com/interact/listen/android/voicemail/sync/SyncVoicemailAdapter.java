package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.DownloadRunnable;
import com.interact.listen.android.voicemail.NotificationHelper;
import com.interact.listen.android.voicemail.Voicemail;
import com.interact.listen.android.voicemail.VoicemailNotifyReceiver;
import com.interact.listen.android.voicemail.client.AccountInfo;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SyncVoicemailAdapter extends AbstractCloudSyncAdapter
{
    private static final String TAG = Constants.TAG + "SyncVoicemail";
    private static final long CLEAN_INTERVAL = 24 * 3600000; // once a day

    public static final String LAST_CLEAN = "last_audio_clean";
    public static final String SERVER_LAST_SYNC = "server_last_sync";
    
    public SyncVoicemailAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
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
                                                VoicemailSyncIter iter, boolean fullList) throws RemoteException
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
    
    private void deleteMissingVoicemails(ContentProviderClient provider, SyncResult syncResult, VoicemailSyncIter iter)
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
    
    private boolean pushVoicemailUpdates(AccountInfo aInfo, ContentProviderClient provider,
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
                if(ClientUtilities.deleteVoicemail(deviceId, aInfo, voicemail.getVoicemailId()))
                {
                    Log.i(TAG, "removing local voicemail after sync " + voicemail.getVoicemailId());
                    VoicemailHelper.deleteVoicemail(provider, voicemail);
                    syncResult.stats.numDeletes++;
                }
            }
            else if(voicemail.isMarkedRead())
            {
                Log.i(TAG, "pushing read voicemail to server: " + voicemail.getVoicemailId());
                boolean succ = ClientUtilities.markVoicemailRead(deviceId, aInfo, voicemail.getVoicemailId(), !voicemail.getIsNew());
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

    
    private boolean updateLocalVoicemails(ContentProviderClient provider, SyncResult syncResult, VoicemailSyncIter iter,
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

    private void downloadAudio(AccountInfo aInfo, ContentProviderClient provider,
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
            downloadTask.setAccount(aInfo.getAccount(), aInfo.getAuthToken());
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
    
    @Override
    public void authoritySync(AccountInfo aInfo, SyncType syncType, Authority auth,
                              ContentProviderClient provider, SyncResult syncResult, SharedPreferences prefs)
        throws AuthorizationException, RemoteException, IOException
    {
        Log.i(TAG, "authority sync " + aInfo.getName() + ": " + syncType + " auth=" + auth.name());

        long lastServerSyncTime = prefs.getLong(SERVER_LAST_SYNC, 0);

        final TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = tm.getDeviceId();

        if(syncType == SyncType.UPLOAD_ONLY)
        {
            Log.v(TAG, "doing update sync");
            List<Voicemail> localVoicemails = VoicemailHelper.getUpdatedVoicemails(provider, aInfo.getName());
            Log.i(TAG, "got updated voicemails for sync: " + localVoicemails);
            pushVoicemailUpdates(aInfo, provider, syncResult, localVoicemails, deviceId);
            Log.i(TAG, "update push completed");
            return;
        }
        
        Long[] syncTime = new Long[] {0L, lastServerSyncTime};
        boolean fullRetrieve = true;
        /*
        if(syncType == SyncType.PERIODIC)
        {
            syncTime[0] = lastServerSyncTime;
            fullRetrieve = false;
        }
        */
        
        List<Voicemail> serverVoicemails = ClientUtilities.retrieveVoicemails(aInfo, syncTime);
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

        List<Voicemail> providerVoicemails = VoicemailHelper.getVoicemails(provider, aInfo.getName(), 0, true);
        Log.i(TAG, "Got voicemails from provider: " + providerVoicemails.size());

        // each list ordered by the voicemail ID on the server
        
        VoicemailSyncIter iter = new VoicemailSyncIter(providerVoicemails, serverVoicemails);
        
        List<Voicemail> needAudio = insertNewVoicemails(aInfo.getAccount(), provider, syncResult, iter, fullRetrieve);
        
        if(fullRetrieve)
        {
            deleteMissingVoicemails(provider, syncResult, iter);
        }

        pushVoicemailUpdates(aInfo, provider, syncResult, providerVoicemails, deviceId);
        updateLocalVoicemails(provider, syncResult, iter, needAudio);
        
        prefs.edit().putLong(SERVER_LAST_SYNC, syncTime[1]).commit();

        if(isInterrupted())
        {
            Log.i(TAG, "sync interrupted");
            return;
        }
        
        downloadAudio(aInfo, provider, syncResult, needAudio);
        cleanAudio(prefs, provider, aInfo.getAccount());

        Log.i(TAG, "synced voicemails");
    }

}
