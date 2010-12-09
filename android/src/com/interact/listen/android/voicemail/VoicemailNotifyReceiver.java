package com.interact.listen.android.voicemail;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

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
            int count = extras.getInt(Constants.EXTRA_COUNT);
            
            if(ids == null && extras.containsKey(Constants.EXTRA_ID))
            {
                ids = new int[]{extras.getInt(Constants.EXTRA_ID)};
            }
            if(ids != null)
            {
                count = Math.max(count, ids.length);
            }
            NotificationHelper.updateNotifications(context, ids, count);
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

    public static void broadcastVoicemailNotification(Context context, ContentProviderClient provider,
                                                      String userName, int id) throws RemoteException
    {
        Log.v(TAG, "broadcast voicemail notification: " + id);

        Intent intent = new Intent(Constants.ACTION_NOTIFY_NEW_VOICEMAILS);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_COUNT, 1);
        intent.putExtra(Constants.EXTRA_ACCOUNT_NAME, userName);
        context.sendBroadcast(intent);
    }

    public static void broadcastVoicemailNotifications(Context context, ContentProviderClient provider,
                                                       String userName, int num) throws RemoteException
    {
        Log.v(TAG, "broadcast " + num + " voicemails");

        Intent intent = new Intent(Constants.ACTION_NOTIFY_NEW_VOICEMAILS);
        intent.putExtra(Constants.EXTRA_COUNT, num);
        intent.putExtra(Constants.EXTRA_ACCOUNT_NAME, userName);
        context.sendBroadcast(intent);
    }

    public static void broadcastConnectionError(Context context, String userName, Throwable e)
    {
        /*
        Intent intent = new Intent(Constants.ACTION_NOTIFY_ERROR);
        if(e != null)
        {
            intent.putExtra(Constants.EXTRA_NOTIFY_ERROR, e.getMessage());
        }
        intent.putExtra(Constants.EXTRA_ACCOUNT_NAME, userName);
        context.sendBroadcast(intent);
        */
    }
}
