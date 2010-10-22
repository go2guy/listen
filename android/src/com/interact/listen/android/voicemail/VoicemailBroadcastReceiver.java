package com.interact.listen.android.voicemail;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.interact.listen.android.voicemail.controller.ControllerAdapter;
import com.interact.listen.android.voicemail.controller.DefaultController;

public class VoicemailBroadcastReceiver extends BroadcastReceiver
{
    public static final String BROADCAST_ACTION_UPDATE = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";

    private static final String TAG = VoicemailBroadcastReceiver.class.getName();
    private static final int LISTEN_NOTIFICATION = 45;
    private ControllerAdapter controller = new ControllerAdapter(new DefaultController());

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.v(TAG, "onReceive(), intent action = [" + intent.getAction() + "]");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            ListenVoicemailService.start(context);
        }
        else if(intent.getAction().equals(BROADCAST_ACTION_UPDATE))
        {
            Bundle extras = intent.getExtras();
            long[] ids = extras.getLongArray("ids");

            updateNotifications(context, ids);
            updateNotificationStatus(context, ids);
        }
        else
        {
            Log.w(TAG, "Unexpected intent: " + intent);
        }
    }

    private void updateNotifications(Context context, long[] ids)
    {
    	int newMessageCount = ids.length;
        Log.v(TAG, "updateNotifications(), count = [" + newMessageCount + "]");

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(ns);

        if(newMessageCount > 0)
        {
            String title = "New Listen Voicemail" + (newMessageCount != 1 ? "s" : "");
            String content = newMessageCount + " new Voicemail" + (newMessageCount != 1 ? "s" : "");

            int icon = R.drawable.notification_bar_icon;
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, title, when);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.vibrate = new long[] {100, 1000};
            //Comment out or change to existing fie on the emulator for dev.  There currently isn't a default
            //notification sound for the emulator.
            notification.defaults |= Notification.DEFAULT_SOUND;

            Intent notificationIntent = new Intent(context, ListenVoicemail.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            notification.setLatestEventInfo(context, title, content, contentIntent);

            // If there is only one new message, don't show the number over the icon
            // notification.number = idsLength > 1 ? idsLength : 0;
            notificationManager.notify(LISTEN_NOTIFICATION, notification);
        }
    }
    
    private void updateNotificationStatus(Context context, long[] ids)
    {
    	controller.markVoicemailsNotified(context, ids);
    }
}
