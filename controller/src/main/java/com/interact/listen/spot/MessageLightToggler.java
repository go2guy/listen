package com.interact.listen.spot;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail.MessageLightState;

public interface MessageLightToggler
{
    public void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber, MessageLightState state);
    public void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber);
    public void toggleMessageLight(PersistenceService persistenceService, Subscriber subscriber);
}
