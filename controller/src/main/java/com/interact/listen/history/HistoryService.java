package com.interact.listen.history;

import com.interact.listen.PersistenceService;
import com.interact.listen.resource.History;
import com.interact.listen.resource.Subscriber;

import java.util.Date;

public class HistoryService
{
    private PersistenceService persistenceService;

    public HistoryService(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
    }

    private void write(History history)
    {
        history.setChannel(persistenceService.getChannel());
        history.setPerformedBySubscriber(persistenceService.getCurrentSubscriber());
        persistenceService.save(history);
    }

    public void writeChangedVoicemailPin(Subscriber onSubscriber, String oldPin, String newPin)
    {
        History history = new History();
        history.setAction("Changed voicemail PIN");
        history.setDescription("Changed voicemail PIN from [" + oldPin + "] to [" + newPin + "]");
        history.setOnSubscriber(onSubscriber);
        write(history);
    }

    public void writeDeletedVoicemail(Subscriber onSubscriber, String voicemailReceivedBy, Date voicemailReceivedDate)
    {
        History history = new History();
        history.setAction("Deleted voicemail");
        history.setDescription("Deleted voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        write(history);
    }

    public void writeDownloadedVoicemail(Subscriber onSubscriber, String voicemailReceivedBy,
                                         Date voicemailReceivedDate)
    {
        History history = new History();
        history.setAction("Downloaded voicemail");
        history.setDescription("Downloaded voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        write(history);
    }

    public void writeDroppedConferenceCaller(String droppedCaller, String conferenceDescription)
    {
        History history = new History();
        history.setAction("Dropped conference caller");
        history.setDescription("Dropped caller [" + droppedCaller + "] from conference [" + conferenceDescription + "]");
        write(history);
    }

    // TODO not yet written anywhere
    public void writeListenedToVoicemail(Subscriber onSubscriber, String voicemailReceivedBy,
                                         Date voicemailReceivedDate)
    {
        History history = new History();
        history.setAction("Listened to voicemail");
        history.setDescription("Listened to voicemail received by [" + voicemailReceivedBy + "] on [" +
                               voicemailReceivedDate + "]");
        history.setOnSubscriber(onSubscriber);
        write(history);
    }

    public void writeMutedConferenceCaller(String mutedCaller, String conferenceDescription)
    {
        History history = new History();
        history.setAction("Muted conference caller");
        history.setDescription("Muted caller [" + mutedCaller + "] in conference [" + conferenceDescription + "]");
        write(history);
    }

    public void writeStartedConference(String conferenceDescription)
    {
        History history = new History();
        history.setAction("Started conference");
        history.setDescription("Started conference [" + conferenceDescription + "]");
        write(history);
    }

    public void writeStartedRecordingConference(String conferenceDescription)
    {
        History history = new History();
        history.setAction("Started recording conference");
        history.setDescription("Started recording conference [" + conferenceDescription + "]");
        write(history);
    }

    public void writeStoppedConference(String conferenceDescription)
    {
        History history = new History();
        history.setAction("Stopped conference");
        history.setDescription("Stopped conference [" + conferenceDescription + "]");
        write(history);
    }

    public void writeStoppedRecordingConference(String conferenceDescription)
    {
        History history = new History();
        history.setAction("Stopped recording conference");
        history.setDescription("Stopped recording conference [" + conferenceDescription + "]");
        write(history);
    }

    public void writeUnmutedConferenceCaller(String unmutedCaller, String conferenceDescription)
    {
        History history = new History();
        history.setAction("Unmuted conference caller");
        history.setDescription("Unmuted caller [" + unmutedCaller + "] in conference [" + conferenceDescription + "]");
        write(history);
    }
}
