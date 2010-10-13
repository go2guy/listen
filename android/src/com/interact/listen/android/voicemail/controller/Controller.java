package com.interact.listen.android.voicemail.controller;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

public interface Controller
{
    // Subscriber interactions
    public Long getSubscriberIdFromUsername(String api, String username) throws ControllerException, ConnectionException, UserNotFoundException;

    // Voicemail interactions
    public List<Voicemail> retrieveVoicemails(String api, Long subscriberId) throws ControllerException, ConnectionException;
    public void markVoicemailsNotified(String api, long[] ids) throws ControllerException, ConnectionException;
    public void markVoicemailsRead(String api, Long[] ids) throws ControllerException, ConnectionException;
    public void deleteVoicemails(String api, Long[] ids) throws ConnectionException;
}
