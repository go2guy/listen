package com.interact.listen.android.voicemail;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

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

        Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
        intent.putExtra(Settings.EXTRA_AUTHORITIES, VoicemailProvider.AUTHORITY);

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
        // test showed that this also caused the deleteIntent to get executed to mark all voicemails notified
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

        Intent notificationIntent = null;
        if(newCount == 1)
        {
            // this breaks down with multiple accounts, but probably isn't an issue
            notificationIntent = new Intent(Constants.ACTION_VIEW_VOICEMAIL);
            notificationIntent.putExtra(Constants.EXTRA_ID, ids[0]);
        }
        else
        {
            notificationIntent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
        }
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification(icon, title, when);

        notification.deleteIntent = PendingIntent.getService(context, 0, new Intent(Constants.ACTION_MARK_NOTIFIED), 0);

        notification.tickerText = context.getString(newCount == 1 ? R.string.new_voicemail_ticker : R.string.new_voicemails_ticker);
        //notification.number = newCount == 1  || newCount > 9 ? 0 : newCount;

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        if(ApplicationSettings.isLightEnabled(context))
        {
            //notification.defaults |= Notification.DEFAULT_LIGHTS;
            notification.ledARGB = 0xff0000ff;
            notification.ledOnMS = 300;
            notification.ledOffMS = 1000;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        }
        
        if(ApplicationSettings.isVibrateEnabled(context))
        {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        
        String ring = ApplicationSettings.getNotificationRing(context);
        if(ring == null)
        {
            notification.audioStreamType = AudioManager.STREAM_NOTIFICATION;
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        else if(ring.length() > 0)
        {
            notification.audioStreamType = AudioManager.STREAM_NOTIFICATION;
            notification.sound = Uri.parse(ring);
        }
        
        notification.setLatestEventInfo(context, title, content, contentIntent);
        
        nManager.notify(VOICEMAIL_NOTIFICATION, notification);
    }
    
    public interface OnConfirm
    {
        void onConfirmed(Voicemail voicemail);
    }
    
    public static void alertDelete(final Context context, final int id, final OnConfirm listener)
    {
        final Voicemail voicemail = VoicemailHelper.getVoicemail(context.getContentResolver(), id);
        if(voicemail == null)
        {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_delete_title);
        builder.setMessage(R.string.dialog_delete_summary);
        builder.setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                VoicemailHelper.moveVoicemailToTrash(context.getContentResolver(), voicemail.getId());
                SyncSchedule.syncUpdate(context, voicemail.getUserName());
                if(listener != null)
                {
                    listener.onConfirmed(voicemail);
                }
            }
        });
        
        builder.setNegativeButton(R.string.dialog_delete_cancel, null);
        builder.setCancelable(true);
        AlertDialog d = builder.create();
        d.show();
    }
    
    public static void dial(Context context, String leftBy)
    {
        if(TextUtils.isEmpty(leftBy))
        {
            return;
        }
        Intent call = new Intent(Intent.ACTION_CALL);
        call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        StringBuilder sb = new StringBuilder();
        sb.append("tel:");

        if(leftBy.length() <= 3)
        {
            String dp = ApplicationSettings.getDialPrefix(context);
            if(!TextUtils.isEmpty(dp))
            {
                sb.append(dp);
                
                if(dp.charAt(dp.length() - 1) != PhoneNumberUtils.PAUSE &&
                    dp.charAt(dp.length() - 1) != PhoneNumberUtils.WAIT)
                {
                    sb.append(PhoneNumberUtils.WAIT);
                }

            }
        }

        sb.append(leftBy);
        
        String dialString = sb.toString();
        Log.i(TAG, "dialing: " + sb.toString());
        
        call.setData(Uri.parse(dialString));
        context.startActivity(call);
    }
}
