package com.interact.listen.android.voicemail.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.Voicemail;

import java.util.ArrayList;
import java.util.List;

public class ControllerAdapter
{
    private static final String TAG = ControllerAdapter.class.getName();
    private static final int LISTEN_COMMUNICATION_ERROR_NOTIFICATION = 46;
    private Controller controller;

    public ControllerAdapter(Controller controller)
    {
        this.controller = controller;
    }

    public Long getSubscriberIdFromUsername(Context context)
    {
        try
        {
            return controller.getSubscriberIdFromUsername(ApplicationSettings.getApi(context),
                                                          ApplicationSettings.getUsername(context));
        }
        catch(ControllerException e)
        {
            notifyConnectionError(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
        catch(ConnectionException e)
        {
            notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
        }
        catch(UserNotFoundException e)
        {
            notifyConnectionError(context, "User not found with id '" + e.getUsername() + "'");
            Log.w(TAG, "User not found on controller with id [" + e.getUsername() + "]", e);
        }

        return -1L;
    }

    public List<Voicemail> retrieveVoicemails(Context context)
    {
        try
        {
            return controller.retrieveVoicemails(ApplicationSettings.getApi(context),
                                                 ApplicationSettings.getSubscriberId(context));
        }
        catch(ControllerException e)
        {
            notifyConnectionError(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
        catch(ConnectionException e)
        {
            notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
        }

        return new ArrayList<Voicemail>();
    }

    public void markVoicemailsNotified(Context context, long[] ids)
    {
        try
        {
            controller.markVoicemailsNotified(ApplicationSettings.getApi(context), ids);
        }
        catch(ControllerException e)
        {
            notifyConnectionError(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
        catch(ConnectionException e)
        {
            notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
        }
    }

    public void markVoicemailsRead(Context context, Long[] ids)
    {
        try
        {
            controller.markVoicemailsRead(ApplicationSettings.getApi(context), ids);
        }
        catch(ControllerException e)
        {
            notifyConnectionError(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
        catch(ConnectionException e)
        {
            notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
        }
    }

    public void deleteVoicemails(Context context, Long[] ids)
    {
        try
        {
            controller.deleteVoicemails(ApplicationSettings.getApi(context), ids);
        }
        catch(ConnectionException e)
        {
            notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
        }
    }

    private void notifyConnectionError(Context context, Throwable t)
    {
        notifyConnectionError(context, t.getMessage());
    }

    private void notifyConnectionError(Context context, String message)
    {
        Log.v(TAG, "notifyConnectionError()");
        String title = "Listen connection error";

        int icon = R.drawable.notification_bar_icon_error;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, title, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        Intent intent = new Intent(context, ApplicationSettings.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, title, message, pIntent);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(LISTEN_COMMUNICATION_ERROR_NOTIFICATION, notification);
    }
}
