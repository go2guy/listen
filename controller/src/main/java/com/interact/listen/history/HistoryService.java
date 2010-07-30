package com.interact.listen.history;

import com.interact.listen.PersistenceService;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryService
{
    private PersistenceService persistenceService;

    private enum Service
    {
        APPLICATION, CONFERENCING, CONFIGURATION, VOICEMAIL;

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

    public void writeChangedAlternatePagerNumber(String alternateNumber)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Changed pager alternate number");
        history.setDescription("Changed pager alternate number to [" + alternateNumber + "]");
        history.setService(Service.CONFIGURATION.toString());
        write(history);
    }

    public void writeChangedVoicemailPin(Subscriber onSubscriber, Long oldPin, Long newPin)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Changed voicemail PIN");
        history.setDescription("Changed [" + onSubscriber.getUsername() + "]'s voicemail PIN from [" + oldPin +
                               "] to [" + newPin + "]");
        history.setOnSubscriber(onSubscriber);
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeCreatedSubscriber(Subscriber subscriber)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Created subscriber");
        history.setDescription("Created subscriber [" + subscriber.getUsername() + "]");
        history.setOnSubscriber(subscriber);
        history.setService(Service.CONFIGURATION.toString());
        write(history);
    }

    public void writeDeletedVoicemail(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Deleted voicemail");
        history.setDescription("Deleted " + getFriendlyVoicemailIdentifier(voicemail));
        history.setOnSubscriber(voicemail.getSubscriber());
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeDownloadedVoicemail(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Downloaded voicemail");
        history.setDescription("Downloaded " + getFriendlyVoicemailIdentifier(voicemail));
        history.setOnSubscriber(voicemail.getSubscriber());
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

    public void writeForwardedVoicemail(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Forwarded voicemail");
        history.setDescription("Forwarded " + getFriendlyVoicemailIdentifier(voicemail));
        history.setService(Service.VOICEMAIL.toString());
        history.setOnSubscriber(voicemail.getSubscriber());
        write(history);
    }

    public void writeLeftVoicemail(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Left voicemail");
        history.setDescription(voicemail.getLeftBy() + " left voicemail for [" +
                               voicemail.getSubscriber().getUsername() + "]");
        history.setOnSubscriber(voicemail.getSubscriber());
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    // TODO not yet written anywhere
    public void writeListenedToVoicemail(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Listened to voicemail");
        history.setDescription("Listened to " + getFriendlyVoicemailIdentifier(voicemail));
        history.setOnSubscriber(voicemail.getSubscriber());
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }

    public void writeLoggedIn(Subscriber subscriber)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Logged in");
        history.setDescription("Logged into GUI");
        history.setOnSubscriber(subscriber);
        history.setService(Service.APPLICATION.toString());
        write(history);
    }

    public void writeLoggedOut(Subscriber subscriber)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Logged out");
        history.setDescription("Logged out of GUI");
        history.setOnSubscriber(subscriber);
        history.setService(Service.APPLICATION.toString());
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
    
    public void writeSentVoicemailAlternatePage(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Sent voicemail alternate number page");
        history.setDescription("Sent page to [" + Configuration.get(Property.Key.ALTERNATE_NUMBER) + "] for " 
                               + getFriendlyVoicemailIdentifier(voicemail));
        history.setService(Service.VOICEMAIL.toString());
        write(history);
    }
    
    public void writeSentVoicemailPage(Voicemail voicemail)
    {
        ActionHistory history = new ActionHistory();
        history.setAction("Sent voicemail page");
        history.setDescription("Sent page to [" + voicemail.getSubscriber().getSmsAddress() + "] for "
                               + getFriendlyVoicemailIdentifier(voicemail));
        history.setService(Service.VOICEMAIL.toString());
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

    private static String getFormattedDate(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    private static String getFriendlyVoicemailIdentifier(Voicemail voicemail)
    {
        return "voicemail for [" + voicemail.getSubscriber().getUsername() + "] from [" + voicemail.getLeftBy() +
               "] left on [" + getFormattedDate(voicemail.getDateCreated()) + "]";
    }
}
