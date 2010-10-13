package com.interact.listen.android.voicemail.controller;

import android.content.Context;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

public class ControllerAdapter
{
    private Controller controller;

    public ControllerAdapter(Controller controller)
    {
        this.controller = controller;
    }

    public Long getSubscriberIdFromUsername(Context context)
    {
        return controller.getSubscriberIdFromUsername(ApplicationSettings.getApi(context),
                                                      ApplicationSettings.getUsername(context));
    }

    public List<Voicemail> retrieveVoicemails(Context context)
    {
        return controller.retrieveVoicemails(ApplicationSettings.getApi(context),
                                             ApplicationSettings.getSubscriberId(context));
    }

    public void markVoicemailsNotified(Context context, long[] ids)
    {
        controller.markVoicemailsNotified(ApplicationSettings.getApi(context), ids);
    }

    public void markVoicemailsRead(Context context, Long[] ids)
    {
        controller.markVoicemailsRead(ApplicationSettings.getApi(context), ids);
    }

    public void deleteVoicemails(Context context, Long[] ids)
    {
        controller.deleteVoicemails(ApplicationSettings.getApi(context), ids);
    }
}
