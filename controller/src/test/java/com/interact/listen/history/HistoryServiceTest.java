package com.interact.listen.history;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenTest;
import com.interact.listen.PersistenceService;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.ActionHistory;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Test;

public class HistoryServiceTest extends ListenTest
{
    private static final Channel CHANNEL = Channel.AUTO;
    private static final String VOICEMAIL_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    private Subscriber subscriber;
    private HistoryService service;

    @Before
    public void setUp()
    {
        subscriber = createSubscriber(session);
        PersistenceService ps = new PersistenceService(session, subscriber, CHANNEL);
        service = new HistoryService(ps);
    }

    @Test
    public void test_writeChangedAlternatePagerNumber_writesHistory()
    {
        String alternateNumber = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeChangedAlternatePagerNumber(alternateNumber);

        assertEquals("Changed pager alternate number", history.getAction());
        assertEquals("Changed pager alternate number to [" + alternateNumber + "]", history.getDescription());
        assertEquals("Configuration", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeChangedVoicemailPin_writesHistory()
    {
        Subscriber onSubscriber = createSubscriber(session);
        String oldPin = TestUtil.randomString();
        String newPin = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeChangedVoicemailPin(onSubscriber, oldPin, newPin);

        assertEquals("Changed voicemail PIN", history.getAction());
        assertEquals("Changed [" + onSubscriber.getUsername() + "]'s voicemail PIN from [" + oldPin + "] to [" +
                     newPin + "]", history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeCreatedSubscriber_writesHistory()
    {
        Subscriber createdSubscriber = createSubscriber(session);
        ActionHistory history = (ActionHistory)service.writeCreatedSubscriber(createdSubscriber);

        assertEquals("Created subscriber", history.getAction());
        assertEquals("Created subscriber [" + createdSubscriber.getUsername() + "]", history.getDescription());
        assertEquals("Configuration", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeDeletedSubscriber_writesHistory()
    {
        Subscriber deletedSubscriber = createSubscriber(session);
        ActionHistory history = (ActionHistory)service.writeDeletedSubscriber(deletedSubscriber);

        assertEquals("Deleted subscriber", history.getAction());
        assertEquals("Deleted subscriber [" + deletedSubscriber.getUsername() + "]", history.getDescription());
        assertEquals("Application", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeDeletedVoicemail_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeDeletedVoicemail(voicemail);

        assertEquals("Deleted voicemail", history.getAction());
        assertEquals("Deleted " + getFriendlyVoicemailIdentifier(voicemail), history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeDownloadedVoicemail_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeDownloadedVoicemail(voicemail);

        assertEquals("Downloaded voicemail", history.getAction());
        assertEquals("Downloaded " + getFriendlyVoicemailIdentifier(voicemail), history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeDroppedConferenceCaller_writesHistory()
    {
        String caller = TestUtil.randomString();
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeDroppedConferenceCaller(caller, description);

        assertEquals("Dropped conference caller", history.getAction());
        assertEquals("Dropped caller [" + caller + "] from conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeForwardedVoicemail_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeForwardedVoicemail(voicemail);

        assertEquals("Forwarded voicemail", history.getAction());
        assertEquals("Forwarded " + getFriendlyVoicemailIdentifier(voicemail), history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeLeftVoicemail_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeLeftVoicemail(voicemail);

        assertEquals("Left voicemail", history.getAction());
        assertEquals(voicemail.getLeftBy() + " left voicemail for [" + subscriber.getUsername() + "]",
                     history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeListenedToVoicemail_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeListenedToVoicemail(voicemail);

        assertEquals("Listened to voicemail", history.getAction());
        assertEquals("Listened to " + getFriendlyVoicemailIdentifier(voicemail), history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeLoggedIn_withActiveDirectoryTrue_writesHistory()
    {
        Subscriber loggedInSubscriber = createSubscriber(session);
        ActionHistory history = (ActionHistory)service.writeLoggedIn(loggedInSubscriber, true);

        assertEquals("Logged in", history.getAction());
        assertEquals("Logged into GUI (Active Directory)", history.getDescription());
        assertEquals("Application", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeLoggedIn_withActiveDirectoryFalse_writesHistory()
    {
        Subscriber loggedInSubscriber = createSubscriber(session);
        ActionHistory history = (ActionHistory)service.writeLoggedIn(loggedInSubscriber, false);

        assertEquals("Logged in", history.getAction());
        assertEquals("Logged into GUI (Local Account)", history.getDescription());
        assertEquals("Application", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeLoggedOut_writesHistory()
    {
        Subscriber loggedOutSubscriber = createSubscriber(session);
        ActionHistory history = (ActionHistory)service.writeLoggedOut(loggedOutSubscriber);

        assertEquals("Logged out", history.getAction());
        assertEquals("Logged out of GUI", history.getDescription());
        assertEquals("Application", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeMutedConferenceCaller_writesHistory()
    {
        String caller = TestUtil.randomString();
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeMutedConferenceCaller(caller, description);

        assertEquals("Muted conference caller", history.getAction());
        assertEquals("Muted caller [" + caller + "] in conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeSentVoicemailAlternatePage_writesHistory()
    {
        String original = Configuration.get(Property.Key.ALTERNATE_NUMBER);

        String number = TestUtil.randomString();
        Configuration.set(Property.Key.ALTERNATE_NUMBER, number);

        try
        {
            Voicemail voicemail = createVoicemail(session, subscriber);
            ActionHistory history = (ActionHistory)service.writeSentVoicemailAlternatePage(voicemail);

            assertEquals("Sent voicemail alternate number page", history.getAction());
            assertEquals("Sent page to [" + number + "] for " + getFriendlyVoicemailIdentifier(voicemail),
                         history.getDescription());
            assertEquals("Voicemail", history.getService());
            assertEquals(CHANNEL, history.getChannel());
            assertEquals(subscriber, history.getSubscriber());
        }
        finally
        {
            Configuration.set(Property.Key.ALTERNATE_NUMBER, original);
        }
    }

    @Test
    public void test_writeSentVoicemailPage_writesHistory()
    {
        Voicemail voicemail = createVoicemail(session, subscriber);
        ActionHistory history = (ActionHistory)service.writeSentVoicemailPage(voicemail);

        assertEquals("Sent voicemail page", history.getAction());
        assertEquals("Sent page to [" + voicemail.getSubscriber().getSmsAddress() + "] for " +
                     getFriendlyVoicemailIdentifier(voicemail), history.getDescription());
        assertEquals("Voicemail", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeStartedConference_writesHistory()
    {
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeStartedConference(description);

        assertEquals("Started conference", history.getAction());
        assertEquals("Started conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeStartedRecordingConference_writesHistory()
    {
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeStartedRecordingConference(description);

        assertEquals("Started recording conference", history.getAction());
        assertEquals("Started recording conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeStoppedConference_writesHistory()
    {
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeStoppedConference(description);

        assertEquals("Stopped conference", history.getAction());
        assertEquals("Stopped conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeStoppedRecordingConference_writesHistory()
    {
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeStoppedRecordingConference(description);

        assertEquals("Stopped recording conference", history.getAction());
        assertEquals("Stopped recording conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    @Test
    public void test_writeUnmutedConferenceCaller_writesHistory()
    {
        String caller = TestUtil.randomString();
        String description = TestUtil.randomString();
        ActionHistory history = (ActionHistory)service.writeUnmutedConferenceCaller(caller, description);

        assertEquals("Unmuted conference caller", history.getAction());
        assertEquals("Unmuted caller [" + caller + "] in conference [" + description + "]", history.getDescription());
        assertEquals("Conferencing", history.getService());
        assertEquals(CHANNEL, history.getChannel());
        assertEquals(subscriber, history.getSubscriber());
    }

    private String getFormattedVoicemailDate(Voicemail voicemail)
    {
        return new SimpleDateFormat(VOICEMAIL_DATE_FORMAT).format(voicemail.getDateCreated());
    }

    private String getFriendlyVoicemailIdentifier(Voicemail voicemail)
    {
        return "voicemail for [" + subscriber.getUsername() + "] from [" + voicemail.getLeftBy() + "] left on [" +
               getFormattedVoicemailDate(voicemail) + "]";
    }
}
