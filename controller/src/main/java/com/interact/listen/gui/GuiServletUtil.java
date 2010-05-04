package com.interact.listen.gui;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.User;

import java.util.ArrayList;

public final class GuiServletUtil
{
    private GuiServletUtil()
    {
        throw new AssertionError("Cannot instantiate utility class GuiServletUtil");
    }

    public static Conference getConferenceFromIdOrUser(String id, User user, PersistenceService persistenceService)
    {
        if(id == null)
        {
            if(user.getConferences().size() > 0)
            {
                return new ArrayList<Conference>(user.getConferences()).get(0);
            }
        }
        else
        {
            return (Conference)persistenceService.get(Conference.class, Long.parseLong(id));
        }
        return null;
    }
}
