package com.interact.listen.android.voicemail.controller;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Voicemail;

import java.util.ArrayList;
import java.util.List;

public class ControllerAdapter
{
    private static final String TAG = ControllerAdapter.class.getName();
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
            toast(context, e);
            Log.e(TAG, "Controller communication error", e);
            return -1L;
        }
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
            toast(context, e);
            Log.e(TAG, "Controller communication error", e);
            return new ArrayList<Voicemail>();
        }
    }

    public void markVoicemailsNotified(Context context, long[] ids)
    {
        try
        {
            controller.markVoicemailsNotified(ApplicationSettings.getApi(context), ids);
        }
        catch(ControllerException e)
        {
            toast(context, e);
            Log.e(TAG, "Controller communication error", e);
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
            toast(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
    }

    public void deleteVoicemails(Context context, Long[] ids)
    {
        try
        {
            controller.deleteVoicemails(ApplicationSettings.getApi(context), ids);
        }
        catch(ControllerException e)
        {
            toast(context, e);
            Log.e(TAG, "Controller communication error", e);
        }
    }

    private void toast(Context context, Throwable t)
    {
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, t.getMessage(), duration);
        toast.show();
    }
}
