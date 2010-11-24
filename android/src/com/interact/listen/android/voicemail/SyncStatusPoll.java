package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.os.AsyncTask;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailProvider;

import java.lang.ref.WeakReference;

public class SyncStatusPoll extends AsyncTask<Void, Boolean, Void> implements SyncStatusObserver
{
    private static final String TAG = Constants.TAG + "SyncPoll";
    
    private WeakReference<Activity> wContext;
    private boolean isActive = false;
    private Object mSyncStatusHandle = null;
    private Object syncObject = new Object();
    
    public SyncStatusPoll(Activity context)
    {
        super();
        wContext = new WeakReference<Activity>(context);
    }
    
    private int getUpdateStatus(Context context)
    {
        if(context == null)
        {
            return 0;
        }
        synchronized(syncObject)
        {
            boolean active = false;
            Account[] accounts = AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account account : accounts)
            {
                if(ContentResolver.isSyncActive(account, VoicemailProvider.AUTHORITY))
                {
                    active = true;
                    break;
                }
            }
            if(isActive == active)
            {
                return 0;
            }
            isActive = active;
            return active ? 1 : -1;
        }
    }

    private boolean isAccurate(boolean active)
    {
        synchronized(syncObject)
        {
            return active == isActive;
        }
    }
    
    @Override
    protected void onPreExecute()
    {
        Log.v(TAG, "SyncStatusPoll onPreExecute()");
        if(mSyncStatusHandle != null)
        {
            mSyncStatusHandle = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE, this);
        }
        onStatusChanged(0xffffffff);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        Log.v(TAG, "SyncStatusPoll doInBackground()");
        while (!Thread.interrupted())
        {
            Context context = wContext.get();
            if(context == null)
            {
                break;
            }
            int updateStatus = getUpdateStatus(context);
            context = null;
            if(updateStatus != 0)
            {
                Log.v(TAG, "updating status: " + updateStatus);
                publishProgress(updateStatus > 0);
            }
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                break;
            }
        }
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Boolean... values)
    {
        if(values != null && values.length > 0)
        {
            boolean active = values[values.length - 1];
            if(isAccurate(active))
            {
                setProgressBar(active);
            }
        }
    }

    @Override
    protected void onCancelled()
    {
        Log.v(TAG, "SyncStatusPoll onCancelled()");
        onEnd();
    }
    
    @Override
    protected void onPostExecute(Void result)
    {
        Log.v(TAG, "SyncStatusPoll onPostExecute()");
        onEnd();
    }
    
    private void onEnd()
    {
        if(mSyncStatusHandle != null)
        {
            ContentResolver.removeStatusChangeListener(mSyncStatusHandle);
            mSyncStatusHandle = null;
        }
        isActive = false;
        setProgressBar(false);
    }
    
    @Override
    public void onStatusChanged(int which)
    {
        Log.v(TAG, "SyncStatusPoll onStatusChanged()");
        int updateStatus = getUpdateStatus(wContext.get());
        if(updateStatus != 0)
        {
            setProgressBar(updateStatus > 0);
        }
    }

    private void setProgressBar(boolean enabled)
    {
        Activity context = wContext.get();
        if(context != null)
        {
            context.setProgressBarIndeterminateVisibility(enabled);
        }
    }

}
