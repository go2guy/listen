package com.interact.listen.android.voicemail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.Authority;
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
        intent.putExtra(Settings.EXTRA_AUTHORITIES, Authority.getAuthorities());

        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, title, message, pIntent);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(LISTEN_COMMUNICATION_ERROR_NOTIFICATION, notification);
    }
    
    public static void clearVoicemailNotifications(Context context)
    {
        Log.v(TAG, "clearVoicemailNotifications");
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(VOICEMAIL_NOTIFICATION);
    }
    
    public static void clearNotificationBar(Context context)
    {
        Log.v(TAG, "clearNotificationBar");
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(LISTEN_COMMUNICATION_ERROR_NOTIFICATION);
        manager.cancel(VOICEMAIL_NOTIFICATION);
        // test showed that this also caused the deleteIntent to get executed to mark all voicemails notified
    }
    
    public static void updateNotifications(Context context, int[] ids, int count)
    {
        Log.v(TAG, "updateNotifications: " + count);

        if(count <= 0)
        {
            return;
        }
        
        NotificationManager nManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        final Resources res = context.getResources();
        String title = res.getQuantityString(R.plurals.new_listen_voicemail, count);
        String content = res.getQuantityString(R.plurals.count_listen_voicemail, count, count);
        
        int icon = R.drawable.notification_bar_icon;
        long when = System.currentTimeMillis();

        Intent notificationIntent = null;

        /* There seems to be problems in which a new notification will carry an old ID, so list all in all cases
        if(count == 1 && ids != null && ids.length > 0)
        {
            // this breaks down with multiple accounts, but probably isn't an issue
            Log.i(TAG, "setting notification ID for single voicemail: " + ids[0]);
            notificationIntent = new Intent(Constants.ACTION_VIEW_VOICEMAIL);
            notificationIntent.putExtra(Constants.EXTRA_ID, ids[0]);
        }
        else
        {
            notificationIntent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
        }
        */
        notificationIntent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
        
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification(icon, title, when);

        notification.deleteIntent = PendingIntent.getService(context, 0, new Intent(Constants.ACTION_MARK_NOTIFIED), 0);
        notification.tickerText = res.getQuantityText(R.plurals.new_voicemail_ticker, count);
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
        boolean preCheck(Voicemail voicemail);
        void onConfirmed(Voicemail voicemail);
    }
    
    public static Dialog createDeleteVoicemailDialog(final Context context, final int id, final OnConfirm listener)
    {
        final Voicemail voicemail = VoicemailHelper.getVoicemail(context.getContentResolver(), id);
        if(voicemail == null)
        {
            return null;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_delete_title);
        builder.setMessage(R.string.dialog_delete_summary);
        builder.setPositiveButton(R.string.dialog_delete_confirm, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(listener != null && !listener.preCheck(voicemail))
                {
                    return;
                }
                VoicemailHelper.moveVoicemailToTrash(context.getContentResolver(), voicemail);
                SyncSchedule.syncUpdates(context, voicemail.getUserName(), Authority.VOICEMAIL);
                if(listener != null)
                {
                    listener.onConfirmed(voicemail);
                }
            }
        });
        
        builder.setNegativeButton(R.string.dialog_delete_cancel, null);
        builder.setCancelable(true);
        return builder.create();
    }
    
    public static String getDialString(Context context, String leftBy, boolean asUri)
    {
        if(TextUtils.isEmpty(leftBy) || (!asUri && leftBy.length() >= 7))
        {
            return leftBy;
        }

        StringBuilder sb = new StringBuilder();
        if(asUri)
        {
            sb.append("tel:");
        }
        
        if(leftBy.length() < 7)
        {
            String dp = ApplicationSettings.getDialPrefix(context);
            if(!TextUtils.isEmpty(dp))
            {
                sb.append(dp);
                if (PhoneNumberUtils.extractPostDialPortion(dp).length() == 0)
                {
                    sb.append(PhoneNumberUtils.WAIT);
                }

            }
        }

        sb.append(leftBy);
        
        return sb.toString();
    }
    
    private static boolean isOfficeNumber(String number)
    {
        return !TextUtils.isEmpty(number) && number.length() < 7;
    }
    
    public static String getDialString(String dialPrefix, String number)
    {
        if(!isOfficeNumber(number))
        {
            return number;
        }

        StringBuilder sb = new StringBuilder();

        if(!TextUtils.isEmpty(dialPrefix))
        {
            sb.append(dialPrefix);
            if (PhoneNumberUtils.extractPostDialPortion(dialPrefix).length() == 0)
            {
                sb.append(PhoneNumberUtils.WAIT);
            }
        }

        sb.append(number);
        
        return sb.toString();
    }
    
    public static void dial(Context context, String leftBy)
    {
        if(TextUtils.isEmpty(leftBy))
        {
            return;
        }
        Intent call = new Intent(Intent.ACTION_CALL);
        call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        String dialString = getDialString(context, leftBy, true);
        Log.i(TAG, "dialing: " + dialString);
        
        call.setData(Uri.parse(dialString));
        context.startActivity(call);
    }

    public static void shareVoicemail(Context context, int id)
    {
        shareVoicemail(context, VoicemailHelper.getVoicemail(context.getContentResolver(), id));
    }
    
    public static void shareVoicemail(Context context, Voicemail voicemail)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType(context.getContentResolver().getType(voicemail.getUri()));

        if(!voicemail.isDownloaded())
        {
            intent.setType("text/plain");
        }
        else
        {
            intent.putExtra(Intent.EXTRA_STREAM, voicemail.getUri());
        }
        
        intent.putExtra(Intent.EXTRA_SUBJECT, voicemail.getAudioTitle());

        final String dateStr = voicemail.getDateCreatedString(context, false, context.getString(R.string.dateCreatedUnknown));
        final String fromStr = voicemail.getLeftBy();
        final String fromName = TextUtils.isEmpty(voicemail.getLeftByName()) ? context.getString(R.string.leftByUnknown) : voicemail.getLeftByName();
        final String durStr = voicemail.getDurationString();
        final String transStr = voicemail.getTranscription();
        final int resId = TextUtils.isEmpty(transStr) ? R.string.voicemail_email_body_no_transcription : R.string.voicemail_email_body;

        intent.putExtra(Intent.EXTRA_TEXT, context.getString(resId, dateStr, fromStr, fromName, durStr, transStr));

        Intent chooser = Intent.createChooser(intent, context.getString(R.string.share_voicemail));
        context.startActivity(chooser);
    }

}
