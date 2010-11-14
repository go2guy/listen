package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;

public class DownloadTask extends AsyncTask<Void, Integer, Boolean>
{
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
        return voicemail.isDownloaded();
    }
    
    @Override
    protected void onCancelled()
    {
        if(player != null)
        {
            player.setControllerEnabled(false);
        }
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
            if(!player.isAudioSet())
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
