package com.interact.listen.android.voicemail;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class NotificationHelper
{
    private static final String TAG = Constants.TAG + "NotifyHelper";

    private static final int VOICEMAIL_NOTIFICATION = 45;
    private static final int LISTEN_COMMUNICATION_ERROR_NOTIFICATION = 46;

    private NotificationHelper()
    {
    }

    public static void notifyConnectionError(Context context, Throwable t)
    {
        notifyConnectionError(context, t.getMessage());
    }
    
    public static void notifyConnectionError(Context context, String message)
    {
        Log.v(TAG, "notifyConnectionError: " + message);
        String title = context.getString(R.string.connection_error_notification_title);

        int icon = R.drawable.notification_bar_icon_error;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, title, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        // TODO: go to account settings
        Intent intent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
        //Intent intent = new Intent(Constants.AUTHORIZE_SETTINGS_ACTION);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, title, message, pIntent);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(LISTEN_COMMUNICATION_ERROR_NOTIFICATION, notification);
    }
    
    public static void clearNotificationBar(Context context)
    {
        Log.v(TAG, "clearNotificationBar");
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(LISTEN_COMMUNICATION_ERROR_NOTIFICATION);
        manager.cancel(VOICEMAIL_NOTIFICATION);
    }
    
    public static void updateNotifications(Context context, int[] ids)
    {
        int newCount = ids.length;
        Log.v(TAG, "updateNotifications: " + newCount);

        if(newCount <= 0)
        {
            return;
        }
        
        NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        int titleID = R.string.new_listen_voicemails;
        int contentFormatID = R.string.count_listen_voicemails;
        if(newCount == 1)
        {
            titleID = R.string.new_listen_voicemail;
            contentFormatID = R.string.count_listen_voicemail;
        }

        String title = context.getString(titleID);
        String content = context.getString(contentFormatID, newCount);
        
        int icon = R.drawable.notification_bar_icon;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, title, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.number = newCount == 1  || newCount > 9 ? 0 : newCount;
        
        // TODO: configure vibrate
        notification.vibrate = new long[] {100, 1000};

        // TODO: configure ringtone
        notification.defaults |= Notification.DEFAULT_SOUND;

        Intent notificationIntent = null;
        if(newCount == 1)
        {
            notificationIntent = new Intent(Constants.ACTION_VIEW_VOICEMAIL);
            notificationIntent.putExtra(Constants.EXTRA_ID, ids[0]);
        }
        else
        {
            notificationIntent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
        }
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, content, contentIntent);

        nManager.notify(VOICEMAIL_NOTIFICATION, notification);
    }
    
}
