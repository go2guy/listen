package com.interact.listen.gui;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;

import java.util.ArrayList;

public final class GuiServletUtil
{
    private GuiServletUtil()
    {
        throw new AssertionError("Cannot instantiate utility class GuiServletUtil");
    }

    public static Conference getConferenceFromIdOrSubscriber(String id, Subscriber subscriber,
                                                             PersistenceService persistenceService)
    {
        if(id == null)
        {
            if(subscriber.getConferences().size() > 0)
            {
                return new ArrayList<Conference>(subscriber.getConferences()).get(0);
            }
        }
        else
        {
            return (Conference)persistenceService.get(Conference.class, Long.parseLong(id));
        }
        return null;
    }
}
