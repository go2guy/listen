package com.interact.listen.android.voicemail;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

public class VoicemailNotifyReceiver extends BroadcastReceiver
{
    private static final String TAG = Constants.TAG + "NotifyReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.v(TAG, "onReceive(), intent action = [" + intent.getAction() + "]");

        Bundle extras = intent.getExtras();

        if(Constants.ACTION_NOTIFY_NEW_VOICEMAILS.equals(intent.getAction()))
        {
            int[] ids = extras.getIntArray(Constants.EXTRA_IDS);
            if(ids == null && extras.containsKey(Constants.EXTRA_ID))
            {
                ids = new int[]{extras.getInt(Constants.EXTRA_ID)};
            }
            NotificationHelper.updateNotifications(context, ids);
        }
        else if(Constants.ACTION_NOTIFY_ERROR.equals(intent.getAction()))
        {
            String errorMessage = extras.getString(Constants.EXTRA_NOTIFY_ERROR);
            NotificationHelper.notifyConnectionError(context, errorMessage);
        }
        else
        {
            Log.w(TAG, "Unexpected intent: " + intent);
        }
    }
    
    public static void broadcastVoicemailNotifications(Context context, ContentProviderClient provider,
                                                       String userName, List<Integer> ids) throws RemoteException
    {
        if(ids == null || ids.isEmpty())
        {
            return;
        }
        
        int[] intIDs = null;
        if(ids.size() <= 10)
        {
            intIDs = new int[ids.size()];
            int idx = 0;
            for(Integer id : ids)
            {
                intIDs[idx++] = id;
            }
            Log.v(TAG, "broadcasted " + intIDs.length + " new voicemails");
        }
        else
        {
            Log.v(TAG, "broadcasted lots of voicemails: " + ids.size());
        }

        Intent intent = new Intent(Constants.ACTION_NOTIFY_NEW_VOICEMAILS);
        intent.putExtra(Constants.EXTRA_IDS, intIDs);
        intent.putExtra(Constants.EXTRA_ACCOUNT_NAME, userName);
        context.sendBroadcast(intent);
    }

    public static void broadcastConnectionError(Context context, String userName, Throwable e)
    {
        Intent intent = new Intent(Constants.ACTION_NOTIFY_ERROR);
        if(e != null)
        {
            intent.putExtra(Constants.EXTRA_NOTIFY_ERROR, e.getMessage());
        }
        intent.putExtra(Constants.EXTRA_ACCOUNT_NAME, userName);
        context.sendBroadcast(intent);
    }
}
