package com.interact.listen.android.voicemail;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.interact.listen.android.voicemail.ListenVoicemail;
import com.interact.listen.android.voicemail.ListenVoicemailService;
import com.interact.listen.android.voicemail.R;

public class VoicemailBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = VoicemailBroadcastReceiver.class.getName();
    private static final int LISTEN_NOTIFICATION = 45;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.v(TAG, "onReceive(), intent action = [" + intent.getAction() + "]");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            ListenVoicemailService.start(context);
        }
        else if(intent.getAction().equals("com.interact.listen.android.voicemail.UPDATE_VOICEMAILS"))
        {
            Bundle extras = intent.getExtras();
            int newMessageCount = extras.getInt("newMessageCount");

            updateNotifications(context, newMessageCount);
        }
        else
        {
            Log.w(TAG, "Unexpected intent: " + intent);
        }
    }

    private void updateNotifications(Context context, int count)
    {
        Log.v(TAG, "updateNotifications(), count = [" + count + "]");

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(ns);

        if(count > 0)
        {
            String title = "New Listen Voicemail" + (count != 1 ? "s" : "");
            String content = count + " new Voicemail" + (count != 1 ? "s" : "");

            int icon = R.drawable.notification_bar_icon;
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, title, when);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            Intent notificationIntent = new Intent(context, ListenVoicemail.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
            notification.setLatestEventInfo(context, title, content, contentIntent);

            // If there is only one new message, don't show the number over the icon
            // notification.number = idsLength > 1 ? idsLength : 0;
            notificationManager.notify(LISTEN_NOTIFICATION, notification);
        }
    }
}
