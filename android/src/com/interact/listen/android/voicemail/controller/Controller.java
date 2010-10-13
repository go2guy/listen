package com.interact.listen.android.voicemail.controller;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

public interface Controller
{
    // Subscriber interactions
    public Long getSubscriberIdFromUsername(String api, String username);

    // Voicemail interactions
    public List<Voicemail> retrieveVoicemails(String api, Long subscriberId);
    public void markVoicemailsNotified(String api, long[] ids);
    public void markVoicemailsRead(String api, Long[] ids);
    public void deleteVoicemails(String api, Long[] ids);
}
