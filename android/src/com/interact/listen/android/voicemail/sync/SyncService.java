package com.interact.listen.android.voicemail.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class SyncService extends Service
{
    private static final Object SYNC_LOCK = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate()
    {
        synchronized (SYNC_LOCK)
        {
            if (sSyncAdapter == null)
            {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return sSyncAdapter.getSyncAdapterBinder();
    }

}
