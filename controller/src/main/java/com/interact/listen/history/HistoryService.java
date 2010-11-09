package com.interact.listen.history;

import com.interact.listen.resource.History;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

public interface HistoryService
{
    public History writeChangedAlternatePagerNumber(String alternateNumber);
    public History writeChangedVoicemailPin(Subscriber onSubscriber, String oldPin, String newPin);
    public History writeCreatedSubscriber(Subscriber subscriber);
    public History writeDeletedSubscriber(Subscriber deletedSubscriber);
    public History writeDeletedVoicemail(Voicemail voicemail);
    public History writeDownloadedVoicemail(Voicemail voicemail);
    public History writeDroppedConferenceCaller(String droppedCaller, String conferenceDescription);
    public History writeForwardedVoicemail(Voicemail voicemail);
    public History writeLeftVoicemail(Voicemail voicemail);
    public History writeListenedToVoicemail(Voicemail voicemail);
    public History writeLoggedIn(Subscriber subscriber, boolean isActiveDirectory);
    public History writeLoggedOut(Subscriber subscriber);
    public History writeMutedConferenceCaller(String mutedCaller, String conferenceDescription);
    public History writeSentVoicemailAlternatePage(Voicemail voicemail);
    public History writeSentVoicemailPage(Voicemail voicemail);
    public History writeStartedConference(String conferenceDescription);
    public History writeStartedRecordingConference(String conferenceDescription);
    public History writeStoppedConference(String conferenceDescription);
    public History writeStoppedRecordingConference(String conferenceDescription);
    public History writeUnmutedConferenceCaller(String unmutedCaller, String conferenceDescription);
}
