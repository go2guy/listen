package com.interact.listen.android.voicemail;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class BackgroundService extends Service
{
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;

    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            onHandleIntent((Intent)msg.obj);
            stopSelf(msg.arg1);
        }
    }

    public BackgroundService(String name)
    {
        super();
        mServiceLooper = null;
        mServiceHandler = null;
        mName = name;
        mRedelivery = false;
    }
    
    public void setIntentRedelivery(boolean enabled)
    {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        HandlerThread thread = new HandlerThread("BackgroundService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Message msg = obtainMessage(intent, startId);
        if(msg != null)
        {
            sendMessage(mServiceHandler, msg);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    
    protected Handler getHandler()
    {
        return mServiceHandler;
    }
    
    protected void sendMessage(Handler handler, Message msg)
    {
        handler.sendMessage(msg);
    }
    
    protected Message obtainMessage(Intent intent, int startId)
    {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        return msg;
    }

    protected abstract void onHandleIntent(Intent intent);
}
