package com.interact.listen.history;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.ActionHistory;
import com.interact.listen.resource.Subscriber;

import java.util.Date;

public class HistoryService
{
    private PersistenceService persistenceService;

    private enum Service
    {
        VOICEMAIL, CONFERENCING;

        @Override
        public String toString()
        {
            return name().substring(0, 1).toUpperCase() + name().substring(1);
        }
    }

    public HistoryService(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    private void write(ActionHistory history)
    {
        history.setChannel(persistenceService.getChannel());
        history.setSubscriber(persistenceService.getCurrentSubscriber());
        persistenceService.save(history);
    }

    public void writeChangedVoicemailPin(Subscriber onSubscriber, Long oldPin, Long newPin)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Changed voicemail PIN");
        history.setDescription("Changed voicemail PIN from [" + oldPin + "] to [" + newPin + "]");
        history.setOnSubscriber(onSubscriber);
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeDeletedVoicemail(Subscriber onSubscriber, String voicemailReceivedBy, Date voicemailReceivedDate)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Deleted voicemail");
        history.setDescription("Deleted voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeDownloadedVoicemail(Subscriber onSubscriber, String voicemailReceivedBy, Date voicemailReceivedDate)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Downloaded voicemail");
        history.setDescription("Downloaded voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeDroppedConferenceCaller(String droppedCaller, String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Dropped conference caller");
        history.setDescription("Dropped caller [" + droppedCaller + "] from conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    // TODO not yet written anywhere
    public void writeListenedToVoicemail(Subscriber onSubscriber, String voicemailReceivedBy, Date voicemailReceivedDate)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Listened to voicemail");
        history.setDescription("Listened to voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeMutedConferenceCaller(String mutedCaller, String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Muted conference caller");
        history.setDescription("Muted caller [" + mutedCaller + "] in conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    public void writeStartedConference(String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Started conference");
        history.setDescription("Started conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    public void writeStartedRecordingConference(String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Started recording conference");
        history.setDescription("Started recording conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    public void writeStoppedConference(String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Stopped conference");
        history.setDescription("Stopped conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    public void writeStoppedRecordingConference(String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Stopped recording conference");
        history.setDescription("Stopped recording conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }

    public void writeUnmutedConferenceCaller(String unmutedCaller, String conferenceDescription)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Unmuted conference caller");
        history.setDescription("Unmuted caller [" + unmutedCaller + "] in conference [" + conferenceDescription + "]");
        history.setService(Service.CONFERENCING.toString());
        write(history);
    }
}
