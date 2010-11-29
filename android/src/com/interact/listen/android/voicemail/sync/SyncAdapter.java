package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.DownloadRunnable;
import com.interact.listen.android.voicemail.Voicemail;
import com.interact.listen.android.voicemail.VoicemailNotifyReceiver;
import com.interact.listen.android.voicemail.authenticator.Authenticator;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;

public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String TAG = Constants.TAG + "SyncAdapter";
    private static final long CLEAN_INTERVAL = 12 * 3600000; // twice a day

    private static long lastAudioClean = 0;

    private final AccountManager mAccountManager;
    
    public SyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
        if(lastAudioClean == 0)
        {
            lastAudioClean = System.currentTimeMillis() - CLEAN_INTERVAL;
        }
    }
    
    private static boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }

    private static final class SyncIter
    {
        private List<Voicemail> localVoicemails,  serverVoicemails;
        
        private Iterator<Voicemail> lIter, sIter;
        private Voicemail lVoicemail, sVoicemail;

        // assumes both lists are sorted by voicemail id
        private SyncIter(List<Voicemail> localVoicemails, List<Voicemail> serverVoicemails)
        {
            this.localVoicemails = localVoicemails;
            this.serverVoicemails = serverVoicemails;

            reset();
        }
        
        public void reset()
        {
            lIter = localVoicemails.iterator();
            sIter = serverVoicemails.iterator();
            lVoicemail = null;
            sVoicemail = null;
        }
        
        public boolean next()
        {
            if(isMissingFromLocal())
            {
                sVoicemail = nextIter(sIter);
            }
            else if(isMissingFromServer())
            {
                lVoicemail = nextIter(lIter);
            }
            else // isMatched() or both null
            {
                sVoicemail = nextIter(sIter);
                lVoicemail = nextIter(lIter);
            }
            
            return lVoicemail != null || sVoicemail != null;
        }
        
        public boolean nextMatch()
        {
            while(next() && lVoicemail != null && sVoicemail != null)
            {
                if(isMatched())
                {
                    return true;
                }
            }
            return false;
        }

        public Voicemail getLocal()
        {
            return lVoicemail;
        }
        public Voicemail getServer()
        {
            return sVoicemail;
        }
        
        public boolean isMissingFromLocal()
        {
            return sVoicemail != null && (lVoicemail == null || lVoicemail.getVoicemailId() > sVoicemail.getVoicemailId());
        }
        
        public boolean isMissingFromServer()
        {
            return lVoicemail != null && (sVoicemail == null || sVoicemail.getVoicemailId() > lVoicemail.getVoicemailId());
        }

        public boolean isMatched()
        {   // isMatched() == (!isMissingFromLocal() && !isMissingFromServer())
            return lVoicemail == null || sVoicemail == null ? false : lVoicemail.getVoicemailId() == sVoicemail.getVoicemailId();
        }
        
        private Voicemail nextIter(Iterator<Voicemail> iter)
        {
            return iter.hasNext() ? iter.next() : null;
        }
    }
    
    private List<Voicemail> insertNewVoicemails(Account account, ContentProviderClient provider, SyncResult syncResult,
                                               SyncIter iter) throws RemoteException
    {
        boolean notifyEnabled = ApplicationSettings.isNotificationEnabled(getContext());
        
        List<Voicemail> newVoicemails = new ArrayList<Voicemail>();

        List<Integer> notifyIDs = new ArrayList<Integer>();
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
                if(VoicemailHelper.insertVoicemail(provider, v) != null)
                {
                    syncResult.stats.numInserts++;
                    newVoicemails.add(v);
                    if(v.needsNotification())
                    {
                        notifyIDs.add(v.getId());
                    }
                }
            }
            else if(iter.isMatched() && iter.getLocal().needsNotification())
            {
                notifyIDs.add(iter.getLocal().getId());
            }
            
        }

        VoicemailNotifyReceiver.broadcastVoicemailNotifications(getContext(), provider, account.name, notifyIDs);
        
        return newVoicemails;
    }
    
    private void deleteMissingVoicemails(ContentProviderClient provider, SyncResult syncResult, SyncIter iter)
        throws RemoteException
    {
        iter.reset();
        while(iter.next())
        {
            if(iter.isMissingFromServer())
            {
                Voicemail v = iter.getLocal();
                Log.i(TAG, "voicemail was deleted on server: " + v.getVoicemailId());
                VoicemailHelper.deleteVoicemail(provider, v);
                syncResult.stats.numDeletes++;
            }
        }
    }
    
    private boolean pushVoicemailUpdates(Uri host, String authToken, Account account, ContentProviderClient provider,
                                         SyncResult syncResult, List<Voicemail> voicemails)
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
                if(ClientUtilities.deleteVoicemail(host, voicemail.getVoicemailId(), account.name, authToken))
                {
                    Log.i(TAG, "removing local voicemail after sync " + voicemail.getVoicemailId());
                    VoicemailHelper.deleteVoicemail(provider, voicemail);
                    syncResult.stats.numDeletes++;
                }
            }
            else if(voicemail.isMarkedRead())
            {
                Log.i(TAG, "pushing read voicemail to server: " + voicemail.getVoicemailId());
                boolean succ = ClientUtilities.markVoicemailRead(host, voicemail.getVoicemailId(), account.name,
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
    
    private void cleanAudio(ContentProviderClient provider) throws RemoteException
    {
        if(!isInterrupted() && System.currentTimeMillis() >= lastAudioClean + CLEAN_INTERVAL)
        {
            Log.i(TAG, "cleaning old voicemails");
            VoicemailHelper.deleteOldAudio(provider, null);
            lastAudioClean = System.currentTimeMillis();
        }
    }
    
    public static boolean isNewSyncSupported()
    {
        return false;
    }
    
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult)
    {
        Log.i(TAG, "on perform sync " + account.name);
        
        int syncType = SyncSchedule.getSyncType(extras);
        
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

            if(syncType == SyncSchedule.SYNC_TYPE_SEND_UPDATES)
            {
                Log.v(TAG, "doing update sync");
                List<Voicemail> localVoicemails = VoicemailHelper.getUpdatedVoicemails(provider, account.name, 0);
                Log.i(TAG, "got updated voicemails for sync: " + localVoicemails);
                pushVoicemailUpdates(host, authToken, account, provider, syncResult, localVoicemails);
                Log.i(TAG, "update push completed");
                return;
            }
            
            List<Voicemail> serverVoicemails = ClientUtilities.retrieveVoicemails(host, userID, account.name, authToken, false);
            Log.i(TAG, "Found voicemails on server: " + serverVoicemails.size());
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted after retrieving voicemails from server");
                return;
            }

            List<Voicemail> providerVoicemails = VoicemailHelper.getVoicemails(provider, account.name, 0, true);
            Log.i(TAG, "Got voicemails from provider: " + providerVoicemails.size());

            // each list ordered by the voicemail ID on the server
            
            SyncIter iter = new SyncIter(providerVoicemails, serverVoicemails);
            
            List<Voicemail> needAudio = insertNewVoicemails(account, provider, syncResult, iter);
            deleteMissingVoicemails(provider, syncResult, iter);
            pushVoicemailUpdates(host, authToken, account, provider, syncResult, providerVoicemails);
            updateLocalVoicemails(provider, syncResult, iter, needAudio);
            
            if(isInterrupted())
            {
                Log.i(TAG, "sync interrupted");
                return;
            }
            
            downloadAudio(host, authToken, account, provider, syncResult, needAudio);
            cleanAudio(provider);

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
