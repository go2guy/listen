package com.interact.listen.android.voicemail;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

import java.util.Set;
import java.util.TreeSet;

public class MarkVoicemailsService extends IntentService
{
    private static final String TAG = Constants.TAG + "Marker";
    
    public MarkVoicemailsService()
    {
        super(TAG);
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
            Log.i(TAG, "marking voicemails notified");
            if(ids == null)
            {
                VoicemailHelper.setVoicemailsNotified(getContentResolver());
            }
            else
            {
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

}
