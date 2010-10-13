package com.interact.listen.android.voicemail;

import android.content.*;
import android.os.IBinder;
import android.util.Log;

public class ListenVoicemailServiceBinder
{
    private static final String TAG = ListenVoicemailServiceBinder.class.getName();

    private Context context;
    private IListenVoicemailService service = null;
    private Runnable afterBind;

    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder boundService)
        {
            Log.v(TAG, "onServiceConnected()");
            service = IListenVoicemailService.Stub.asInterface((IBinder)boundService);
            if(afterBind != null)
            {
                Log.v(TAG, "executing afterBind callback");
                afterBind.run();
                afterBind = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className)
        {
            Log.v(TAG, "onServiceDisconnected()");
            service = null;
        }
    };

    public ListenVoicemailServiceBinder(Context context)
    {
        Log.v(TAG, "new ListenVoicemailServiceBinder()");
        this.context = context;
    }

    public void bind()
    {
        bind(null);
    }

    public void bind(Runnable afterBind)
    {
        Log.v(TAG, "bind()");
        Intent intent = new Intent();
        intent.setClassName("com.interact.listen.android.voicemail",
                            "com.interact.listen.android.voicemail.ListenVoicemailService");
        this.afterBind = afterBind;
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void unbind()
    {
        Log.v(TAG, "unbind()");
        context.unbindService(connection);
    }

    public IListenVoicemailService getService()
    {
        Log.v(TAG, "getService()");
        return service;
    }
}
