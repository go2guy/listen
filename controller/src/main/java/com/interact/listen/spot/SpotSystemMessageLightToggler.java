package com.interact.listen.spot;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.resource.Voicemail.MessageLightState;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class SpotSystemMessageLightToggler implements MessageLightToggler, Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SpotSystemMessageLightToggler.class);

    @Override
    public void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber,
                                   MessageLightState state)
    {
        SpotSystem spotSystem = new SpotSystem(persistenceService.getCurrentSubscriber());
        try
        {
            if(state == MessageLightState.ON)
            {
                spotSystem.turnMessageLightOn(accessNumber);
            }
            else
            {
                spotSystem.turnMessageLightOff(accessNumber);
            }
        }
        catch(SpotCommunicationException e)
        {
            LOG.error(e);
        }
        catch(IOException e)
        {
            LOG.error(e);
        }
    }

    @Override
    public void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber)
    {
        boolean hasNew = Voicemail.countNewBySubscriber(persistenceService.getSession(), accessNumber.getSubscriber()) > 0;
        toggleMessageLight(persistenceService, accessNumber, hasNew ? MessageLightState.ON : MessageLightState.OFF);
    }

    @Override
    public void toggleMessageLight(PersistenceService persistenceService, Subscriber subscriber)
    {
        Session session = persistenceService.getSession();
        List<AccessNumber> numbers = AccessNumber.queryBySubscriberWhereSupportsMessageLightTrue(session, subscriber);
        for(AccessNumber accessNumber : numbers)
        {
            toggleMessageLight(persistenceService, accessNumber);
        }
    }
}
