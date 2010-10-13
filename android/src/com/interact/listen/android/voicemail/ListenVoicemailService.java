package com.interact.listen.android.voicemail;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;

import com.interact.listen.android.voicemail.controller.ControllerAdapter;
import com.interact.listen.android.voicemail.controller.DefaultController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListenVoicemailService extends Service
{
    private static final String TAG = "ListenVoicemailService";
    private static final String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    private static final Long VOICEMAIL_POLL_INTERVAL = 30000L;

    private Handler serviceHandler;
    
    private List<Voicemail> mVoicemails = new ArrayList<Voicemail>(10);
    private ControllerAdapter controller = new ControllerAdapter(new DefaultController());

    private Runnable voicemailPollingTask = new Runnable()
    {
        @Override
        public void run()
        {
            Log.v(TAG, "ListenVoicemailService.VoicemailPollingTask.run()");
            retrieveVoicemails();
            serviceHandler.postDelayed(this, VOICEMAIL_POLL_INTERVAL);
        }
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.v(TAG, "onBind()");
        return new IListenVoicemailService.Stub()
        {
            @Override
            public void deleteVoicemail(long id)
            {
                new AsyncTask<Long, Integer, Void>() { 
                    @Override
                    protected Void doInBackground(Long... ids)
                    {
                        controller.deleteVoicemails(ListenVoicemailService.this, ids);
                        return null;
                    }
                }.execute(id);
            }

            @Override
            public List<Voicemail> getVoicemails()
            {
                return mVoicemails;
            }

            @Override
            public void markVoicemailOld(long id)
            {
                new AsyncTask<Long, Integer, Void>() {
                    @Override
                    protected Void doInBackground(Long... ids)
                    {
                        controller.markVoicemailsRead(ListenVoicemailService.this, ids);
                        return null;
                    }
                }.execute(id);
            }

            @Override
            public void markVoicemailsNotified(long[] ids)
            {
                stopPolling();
                controller.markVoicemailsNotified(ListenVoicemailService.this, ids);
                startPolling();
            }

            @Override
            public void startPolling()
            {
                ListenVoicemailService.this.startPolling();
            }
            
            @Override
            public void stopPolling()
            {
                ListenVoicemailService.this.stopPolling();
            }
        };
    }
    
    @Override
    public void onCreate()
    {
        Log.v(TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();

        stopPolling();
        serviceHandler = null;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        Log.v(TAG, "onStart()");
        super.onStart(intent, startId);

        serviceHandler = new Handler();
        startPolling();
    }

    private long[] getIdsToUpdate()
    {
        //Should protect us from a poll of the server disrupting the looping we do below
        ArrayList<Voicemail> voicemails = new ArrayList<Voicemail>(mVoicemails);
        ArrayList<Voicemail> tempVoicemails = new ArrayList<Voicemail>();
        Collections.copy(voicemails, mVoicemails);
        
        for(Voicemail voicemail : voicemails)
        {
            if(!voicemail.getHasNotified())
            {
                tempVoicemails.add(voicemail);
            }
        }
        
        long[] idsToUpdate = new long[tempVoicemails.size()];
        for(int i = 0; i < tempVoicemails.size(); i++)
        {
            idsToUpdate[i] = tempVoicemails.get(i).getId();
        }
        
        return idsToUpdate;
    }
    
    private int getNewMessageCount()
    {
        int numNew = 0;
        
        for(Voicemail voicemail : mVoicemails)
        {
            if(voicemail.getIsNew())
            {
                numNew++;
            }
        }
        
        return numNew;
    }

    private void startPolling()
    {
        Log.v(TAG, "startPolling()");
        serviceHandler.postDelayed(voicemailPollingTask, 100L);
    }

    private void stopPolling()
    {
        Log.v(TAG, "stopPolling()");
        serviceHandler.removeCallbacks(voicemailPollingTask);
    }

    private void retrieveVoicemails()
    {
        mVoicemails = controller.retrieveVoicemails(ListenVoicemailService.this);

        Bundle bundle = new Bundle();
        bundle.putLongArray("ids", getIdsToUpdate());
        bundle.putInt("newMessageCount", getNewMessageCount());

        Intent i = new Intent(UPDATE_ACTION_STRING);
        i.putExtras(bundle);

        sendOrderedBroadcast(i, null);
    }

    /**
     * Starts the Voicemail Service
     * 
     * @param context
     */
    public static void start(Context context)
    {
        Log.v(TAG, "start()");
        ComponentName c = new ComponentName("com.interact.listen.android.voicemail",
                                            "com.interact.listen.android.voicemail.ListenVoicemailService");
        ComponentName service = context.startService(new Intent().setComponent(c));
        if(service == null)
        {
            Log.e(TAG, "Error starting service, service is null");
        }
    }
}
