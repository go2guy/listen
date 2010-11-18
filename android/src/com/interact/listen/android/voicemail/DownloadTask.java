package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;

public class DownloadTask extends AsyncTask<Void, Integer, Boolean>
{
    private static final String TAG = Constants.TAG + "DownloadTask";
    
    private DownloadRunnable runnable;
    private VoicemailPlayer player;
    private Context context;
    private Voicemail voicemail;
    
    public DownloadTask(Context context, VoicemailPlayer player, Voicemail voicemail)
    {
        runnable = new DownloadRunnable(context, voicemail);
        runnable.setProgressListener(new DownloadRunnable.OnProgressUpdate()
        {
            @Override
            public void onProgressUpdate(int percent)
            {
                publishProgress(new Integer[]{percent});
            }
        });

        this.player = player;
        this.context = context;
        this.voicemail = voicemail;
    }

    public void setAccount(Account account, String authToken)
    {
        runnable.setAccount(account, authToken);
    }
    
    @Override
    protected Boolean doInBackground(Void... v)
    {
        runnable.run();

        if(!voicemail.isDownloaded())
        {
            // must re-check in case the sync adapter beat us to it, but not till after we originally got it
            voicemail = VoicemailHelper.getVoicemail(context.getContentResolver(), voicemail.getId());
            if(voicemail == null)
            {
                return false;
            }
        }
        
        if(!voicemail.isDownloading())
        {
            return voicemail.isDownloaded();
        }
        
        Log.i(TAG, "waiting for download to complete");
        LooperThread looper = new LooperThread(context, voicemail);
        looper.start();

        while(!Thread.currentThread().isInterrupted() && looper.isDownloading())
        {
            try
            {
                looper.check();
                looper.join(2000);
                if(!looper.isAlive())
                {
                    Log.i(TAG, "joined looper");
                    break;
                }
            }
            catch(InterruptedException e)
            {
                break;
            }
        }
        looper.interrupt();
        return looper.isDownloaded();
    }
    
    private static class LooperThread extends Thread
    {
        private Context context;
        private Voicemail voicemail;
        private Object syncObject;
        private VoicemailObserver observer;
        
        public LooperThread(Context context, Voicemail voicemail)
        {
            this.context = context;
            this.voicemail = voicemail;
            this.syncObject = new Object();
            this.observer = null;
        }
        
        public boolean isDownloading()
        {
            synchronized(syncObject)
            {
                return voicemail != null && voicemail.isDownloading();
            }
        }

        public boolean isDownloaded()
        {
            Log.i(TAG, "isDownloaded() " + voicemail);
            synchronized(syncObject)
            {
                return voicemail != null && voicemail.isDownloaded();
            }
        }

        public void check()
        {
            if(observer != null)
            {
                observer.dispatchChange(true);
            }
        }
        
        public boolean updateVoicemail()
        {
            synchronized(syncObject)
            {
                if(voicemail != null)
                {
                    voicemail = VoicemailHelper.getVoicemail(context.getContentResolver(), voicemail.getId());
                }
                Log.v(TAG, "updated voicemail" + voicemail);
                return voicemail != null && voicemail.isDownloading();
            }
        }

        public void cancel()
        {
            Log.v(TAG, "stopping wait for download thread");
            synchronized(syncObject)
            {
                if(context != null && observer != null)
                {
                    context.getContentResolver().unregisterContentObserver(observer);
                }
                
                context = null;
                observer = null;
            }
            
            Looper.myLooper().quit();
        }
        
        @Override
        public void run()
        {
            Looper.prepare();
 
            ContentResolver resolver = context.getContentResolver();
            Uri uri = VoicemailHelper.getVoicemailUri(voicemail.getId());
            observer = new VoicemailObserver(this);
            resolver.registerContentObserver(uri, false, observer);
            
            Looper.loop();
        }
        
        private static class VoicemailObserver extends ContentObserver
        {
            private LooperThread looper;
            
            public VoicemailObserver(LooperThread looper)
            {
                super(new Handler());
                this.looper = looper;
            }
            
            @Override
            public void onChange(boolean selfChange)
            {
                if(!looper.updateVoicemail())
                {
                    looper.cancel();
                }
            }
        }
    }
    
    @Override
    protected void onCancelled()
    {
    }
    
    @Override
    protected void onPostExecute(Boolean result)
    {
        if(player == null)
        {
            return;
        }
        
        if(result == null || !result.booleanValue())
        {
            player.setControllerEnabled(false);
        }
        else
        {
            player.setControllerEnabled(true);
            if(!player.isAudioSet() && voicemail != null)
            {
                player.setAudioURI(context, voicemail.getUri());
            }
        }
    }
    
    @Override
    protected void onPreExecute()
    {
        if(player != null)
        {
            player.setLoading();
        }
        
    }
    
    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if(values == null || values.length == 0 || player == null)
        {
            return;
        }
        player.updateBufferPercentage(values[0]);
    }
    
}
