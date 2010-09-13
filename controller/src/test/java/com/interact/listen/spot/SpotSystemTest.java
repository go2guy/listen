package com.interact.listen.spot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.resource.*;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

public class SpotSystemTest extends ListenTest
{
    private SpotSystem spotSystem;
    private HttpClient httpClient;
    private StatSender statSender;

    private Participant participant;

    private final String httpInterfaceUri = "http://www.example.com";
    private final String postStringAddition = "/ccxml/createsession";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");

    @Before
    public void setUp()
    {
        httpClient = mock(HttpClient.class);
        statSender = mock(StatSender.class);

        spotSystem = new SpotSystem(httpInterfaceUri, null);
        spotSystem.setHttpClient(httpClient);
        spotSystem.setStatSender(statSender);

        participant = new Participant();
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
    }

    @Test
    public void test_deleteArtifact_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final String artifact = "file.ext";
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "FILE");
        expectedParameters.put("application", "DEL_ARTIFACT");
        expectedParameters.put("artifact", artifact);
        expectedParameters.put("initiatingChannel", "GUI");

        SpotSystemInvocation invocation = getInvocationForDeleteArtifact(artifact);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_deleteArtifact_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteArtifact(TestUtil.randomString());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_deleteArtifact_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteArtifact(TestUtil.randomString());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_deleteArtifact_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteArtifact(TestUtil.randomString());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_deleteAllSubscriberArtifacts_invokesPostWithParams() throws SpotCommunicationException,
        IOException
    {
        Subscriber subscriber = createSubscriber(session);
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "SUB");
        expectedParameters.put("application", "DEL_ARTIFACT");
        expectedParameters.put("artifact", String.valueOf(subscriber.getId()));
        expectedParameters.put("initiatingChannel", "GUI");

        SpotSystemInvocation invocation = getInvocationForDeleteAllSubscriberArtifacts(subscriber);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_deleteAllSubscriberArtifacts_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteAllSubscriberArtifacts(createSubscriber(session));
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_deleteAllSubscriberArtifacts_whenSpotRespondsWith200_sendsStat()
        throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteAllSubscriberArtifacts(createSubscriber(session));
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_deleteAllSubscriberArtifacts_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDeleteAllSubscriberArtifacts(createSubscriber(session));
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_dropParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "DROP");
        expectedParameters.put("application", "CONF_EVENT");
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("sessionId", participant.getSessionID());
        SpotSystemInvocation invocation = getInvocationForDropParticipant(participant);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_dropParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDropParticipant(participant);
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_dropParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForDropParticipant(participant);
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_dropParticipant_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForDropParticipant(participant);
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_muteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "MUTE");
        expectedParameters.put("application", "CONF_EVENT");
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("sessionId", participant.getSessionID());
        SpotSystemInvocation invocation = getInvocationForMuteParticipant(participant);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_muteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForMuteParticipant(participant);
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_muteParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForMuteParticipant(participant);
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_muteParticipant_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForMuteParticipant(participant);
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_outdial_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final String ani = TestUtil.randomString();
        final Long conferenceId = TestUtil.randomNumeric(9);
        final String destination = TestUtil.randomString();
        final String sessionId = TestUtil.randomString();
        final String interrupt = TestUtil.randomString();

        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "DIAL");
        expectedParameters.put("ani", ani);
        expectedParameters.put("application", "AUTO_DIAL");
        expectedParameters.put("conferenceId", String.valueOf(conferenceId));
        expectedParameters.put("destination", destination);
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("sessionId", sessionId);
        expectedParameters.put("interrupt", interrupt);
        SpotSystemInvocation invocation = getInvocationForOutdial(destination, sessionId, conferenceId, ani, interrupt);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_outdial_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForOutdial(TestUtil.randomString(), TestUtil.randomString(),
                                       TestUtil.randomNumeric(8), TestUtil.randomString(), TestUtil.randomString());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_outdial_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForOutdial(TestUtil.randomString(), TestUtil.randomString(),
                                       TestUtil.randomNumeric(8), TestUtil.randomString(), TestUtil.randomString());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_outdial_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForOutdial(TestUtil.randomString(), TestUtil.randomString(),
                                       TestUtil.randomNumeric(8), TestUtil.randomString(), TestUtil.randomString());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_startRecording_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final Conference conference = getConferenceForRecordingTest();
        conference.setIsStarted(true);
        conference.setStartTime(new Date());
        conference.setRecordingSessionId(TestUtil.randomString());
        final String sessionId = TestUtil.randomString();

        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "START");
        expectedParameters.put("application", "RECORD");
        expectedParameters.put("arcadeId", conference.getArcadeId());
        expectedParameters.put("conferenceId", String.valueOf(conference.getId()));
        expectedParameters.put("description", conference.getDescription());
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("recordingSessionId", conference.getRecordingSessionId());
        expectedParameters.put("sessionId", sessionId);
        expectedParameters.put("startTime", sdf.format(conference.getStartTime()));
        SpotSystemInvocation invocation = getInvocationForStartRecording(conference, sessionId);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_stopRecording_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForStartRecording(getConferenceForRecordingTest(),
                                                                         TestUtil.randomString());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_stopRecording_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForStartRecording(getConferenceForRecordingTest(),
                                                                         TestUtil.randomString());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_stopRecording_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForStopRecording(getConferenceForRecordingTest(),
                                                                        TestUtil.randomString());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_stopRecording_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final Conference conference = getConferenceForRecordingTest();
        conference.setIsStarted(true);
        conference.setStartTime(new Date());
        conference.setRecordingSessionId(TestUtil.randomString());
        final String sessionId = TestUtil.randomString();

        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "STOP");
        expectedParameters.put("application", "RECORD");
        expectedParameters.put("arcadeId", conference.getArcadeId());
        expectedParameters.put("conferenceId", String.valueOf(conference.getId()));
        expectedParameters.put("description", conference.getDescription());
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("recordingSessionId", conference.getRecordingSessionId());
        expectedParameters.put("sessionId", sessionId);
        expectedParameters.put("startTime", sdf.format(conference.getStartTime()));
        SpotSystemInvocation invocation = getInvocationForStopRecording(conference, sessionId);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_startRecording_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForStopRecording(getConferenceForRecordingTest(),
                                                                        TestUtil.randomString());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_startRecording_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException, IOException
    {
        SpotSystemInvocation invocation = getInvocationForStopRecording(getConferenceForRecordingTest(),
                                                                        TestUtil.randomString());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_startRecording_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForStopRecording(getConferenceForRecordingTest(),
                                                                        TestUtil.randomString());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_turnMessageLightOff_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final AccessNumber accessNumber = getAccessNumberForMessageLightTest();
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "OFF");
        expectedParameters.put("application", "MSG_LIGHT");
        expectedParameters.put("destination", accessNumber.getNumber());
        expectedParameters.put("initiatingChannel", "GUI");
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOff(accessNumber);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_turnMessageLightOff_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOff(getAccessNumberForMessageLightTest());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_turnMessageLightOff_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException,
        IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOff(getAccessNumberForMessageLightTest());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_turnMessageLightOff_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOff(getAccessNumberForMessageLightTest());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_turnMessageLightOn_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        final AccessNumber accessNumber = getAccessNumberForMessageLightTest();
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "ON");
        expectedParameters.put("application", "MSG_LIGHT");
        expectedParameters.put("destination", accessNumber.getNumber());
        expectedParameters.put("initiatingChannel", "GUI");
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOn(accessNumber);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_turnMessageLightOn_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOn(getAccessNumberForMessageLightTest());
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_turnMessageLightOn_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException,
        IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOn(getAccessNumberForMessageLightTest());
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_turnMessageLightOn_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForTurnMessageLightOn(getAccessNumberForMessageLightTest());
        testForStatWhenSpotCommunicationException(invocation);
    }

    @Test
    public void test_unmuteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> expectedParameters = new TreeMap<String, String>();
        expectedParameters.put("action", "UNMUTE");
        expectedParameters.put("application", "CONF_EVENT");
        expectedParameters.put("initiatingChannel", "GUI");
        expectedParameters.put("sessionId", participant.getSessionID());
        SpotSystemInvocation invocation = getInvocationForUnmuteParticipant(participant);
        testForExpectedPostArgumentsWhenSuccessful(invocation, expectedParameters);
    }

    @Test
    public void test_unmuteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForUnmuteParticipant(participant);
        testForSpotCommunicationExceptionWhenBadRequest(invocation);
    }

    @Test
    public void test_unmuteParticipant_whenSpotRespondsWith200_sendsStat() throws SpotCommunicationException,
        IOException
    {
        SpotSystemInvocation invocation = getInvocationForUnmuteParticipant(participant);
        testForStatOnSuccessfulResponse(invocation);
    }

    @Test
    public void test_unmuteParticipant_whenSpotRespondsWith400_sendsStat() throws IOException
    {
        SpotSystemInvocation invocation = getInvocationForUnmuteParticipant(participant);
        testForStatWhenSpotCommunicationException(invocation);
    }

    // helper test methods

    private SpotSystemInvocation getInvocationForDeleteAllSubscriberArtifacts(final Subscriber subscriber)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.deleteAllSubscriberArtifacts(subscriber);
            }
        };
    }

    private SpotSystemInvocation getInvocationForDeleteArtifact(final String artifact)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.deleteArtifact(artifact);
            }
        };
    }

    private SpotSystemInvocation getInvocationForDropParticipant(final Participant participant)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.dropParticipant(participant);
            }
        };
    }

    private SpotSystemInvocation getInvocationForMuteParticipant(final Participant participant)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.muteParticipant(participant);
            }
        };
    }

    private SpotSystemInvocation getInvocationForOutdial(final String destination, final String sessionId,
                                                         final Long conferenceId, final String ani, final String interrupt)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.outdial(destination, sessionId, conferenceId, ani, interrupt);
            }
        };
    }

    private SpotSystemInvocation getInvocationForStartRecording(final Conference conference, final String sessionId)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.startRecording(conference, sessionId);
            }
        };
    }

    private SpotSystemInvocation getInvocationForStopRecording(final Conference conference, final String sessionId)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.stopRecording(conference, sessionId);
            }
        };
    }

    private SpotSystemInvocation getInvocationForTurnMessageLightOff(final AccessNumber accessNumber)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.turnMessageLightOff(accessNumber);
            }
        };
    }

    private SpotSystemInvocation getInvocationForTurnMessageLightOn(final AccessNumber accessNumber)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.turnMessageLightOn(accessNumber);
            }
        };
    }

    private SpotSystemInvocation getInvocationForUnmuteParticipant(final Participant participant)
    {
        return new SpotSystemInvocation()
        {
            @Override
            public void invoke() throws SpotCommunicationException, IOException
            {
                spotSystem.unmuteParticipant(participant);
            }
        };
    }

    private void testForStatWhenSpotCommunicationException(SpotSystemInvocation invocation) throws IOException
    {
        try
        {
            invocation.invoke();
        }
        catch(SpotCommunicationException e)
        {
            // expected
        }
        verify(statSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    private void testForStatOnSuccessfulResponse(SpotSystemInvocation invocation) throws IOException,
        SpotCommunicationException
    {
        when(httpClient.getResponseStatus()).thenReturn(200);
        invocation.invoke();
        verify(statSender).send(Stat.PUBLISHED_EVENT_TO_SPOT);
    }

    private void testForSpotCommunicationExceptionWhenBadRequest(SpotSystemInvocation invocation) throws IOException
    {
        when(httpClient.getResponseStatus()).thenReturn(400);
        try
        {
            invocation.invoke();
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            String expected = "Received HTTP Status 400 from SPOT System at [";
            expected += httpInterfaceUri + postStringAddition + "]";
            assertEquals(expected, e.getMessage());
        }
    }

    private void testForExpectedPostArgumentsWhenSuccessful(SpotSystemInvocation invocation,
                                                            Map<String, String> expectedImportedValue)
        throws SpotCommunicationException, IOException
    {
        Map<String, String> expected = new TreeMap<String, String>();
        expected.put("II_SB_importedValue", JSONValue.toJSONString(expectedImportedValue));
        expected.put("uri", "/interact/apps/iistart.ccxml");

        when(httpClient.getResponseStatus()).thenReturn(200);
        invocation.invoke();
        verify(httpClient).post(httpInterfaceUri + postStringAddition, expected);
    }

    private Conference getConferenceForRecordingTest()
    {
        Conference conference = createConference(session, createSubscriber(session));
        conference.setIsStarted(true);
        conference.setStartTime(new Date());
        conference.setRecordingSessionId(TestUtil.randomString());
        return conference;
    }

    private AccessNumber getAccessNumberForMessageLightTest()
    {
        AccessNumber number = new AccessNumber();
        number.setNumber(TestUtil.randomString());
        return number;
    }

    private interface SpotSystemInvocation
    {
        public void invoke() throws SpotCommunicationException, IOException;
    }
}
