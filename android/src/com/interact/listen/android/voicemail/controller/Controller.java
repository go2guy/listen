package com.interact.listen.android.voicemail.controller;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

public interface Controller
{
    // Subscriber interactions
    public Long getSubscriberIdFromUsername(String api, String username) throws ControllerException;

    // Voicemail interactions
    public List<Voicemail> retrieveVoicemails(String api, Long subscriberId) throws ControllerException;
    public void markVoicemailsNotified(String api, long[] ids) throws ControllerException;
    public void markVoicemailsRead(String api, Long[] ids) throws ControllerException;
    public void deleteVoicemails(String api, Long[] ids) throws ControllerException;
}
