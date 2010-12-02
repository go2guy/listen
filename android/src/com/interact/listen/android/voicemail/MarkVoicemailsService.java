package com.interact.listen.android.voicemail;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

import java.util.Set;
import java.util.TreeSet;

public class MarkVoicemailsService extends BackgroundService
{
    private static final String TAG = Constants.TAG + "Marker";
    
    private static final int NOTIFY_ALL  = 1;
    private static final int NOTIFY_SPEC = 2;
    private static final int MARK_READ   = 3;
    
    public MarkVoicemailsService()
    {
        super(TAG);
    }

    @Override
    protected Message obtainMessage(Intent intent, int startId)
    {
        int what = getWhat(intent);
        if(what == 0)
        {
            return null;
        }
        
        Message msg = super.obtainMessage(intent, startId);
        msg.what = what;

        if(what == NOTIFY_ALL)
        {
            Handler handler = getHandler();
            if(handler != null)
            {
                Log.v(TAG, "removing pending notify all's");
                handler.removeMessages(what);
            }
        }
        
        return msg;
    }
    
    @Override
    protected void sendMessage(Handler handler, Message msg)
    {
        if(msg.what == NOTIFY_ALL)
        {
            handler.sendMessageDelayed(msg, 1000);
        }
        else
        {
            handler.sendMessage(msg);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();

        int[] ids = null;
        if(extras != null)
        {
            if(extras.containsKey(Constants.EXTRA_IDS))
            {
                ids = extras.getIntArray(Constants.EXTRA_IDS);
            }
            else if(extras.containsKey(Constants.EXTRA_ID))
            {
                ids = new int[]{extras.getInt(Constants.EXTRA_ID)};
            }
        }
        
        if(Constants.ACTION_MARK_NOTIFIED.equals(intent.getAction()))
        {
            if(ids == null)
            {
                Log.i(TAG, "marking all voicemails notified");
                VoicemailHelper.setVoicemailsNotified(getContentResolver());
            }
            else
            {
                Log.i(TAG, "marking voicemails notified: " + ids.length);
                VoicemailHelper.setVoicemailsNotified(getContentResolver(), ids);
            }
        }
        else if(Constants.ACTION_MARK_READ.equals(intent.getAction()))
        {
            if(ids == null)
            {
                Log.e(TAG, "ids not set for marking read");
            }
            else
            {
                boolean isRead = extras == null ? true : extras.getBoolean(Constants.EXTRA_IS_READ, true);
                Set<String> userNames = new TreeSet<String>();
                ContentResolver resolver = getContentResolver();

                for(int id : ids)
                {
                    Voicemail v = VoicemailHelper.getVoicemail(resolver, id);
                    if(v != null && v.getIsNew() != !isRead)
                    {
                        Log.i(TAG, "marking voicmeail read: " + v.getId());
                        VoicemailHelper.markVoicemailRead(resolver, v, isRead);
                        userNames.add(v.getUserName());
                    }
                }
                for(String name : userNames)
                {
                    if(name != null)
                    {
                        SyncSchedule.syncUpdate(this, name);
                    }
                }
            }
        }

    }

    private int getWhat(Intent intent)
    {
        if(Constants.ACTION_MARK_READ.equals(intent.getAction()))
        {
            return MARK_READ;
        }
        if(Constants.ACTION_MARK_NOTIFIED.equals(intent.getAction()))
        {
            Bundle extras = intent.getExtras();
            if(extras == null || (!extras.containsKey(Constants.EXTRA_IDS) && !extras.containsKey(Constants.EXTRA_ID)))
            {
                return NOTIFY_ALL;
            }
            return NOTIFY_SPEC;
        }
        return 0;
    }

}
