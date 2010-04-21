package com.interact.listen.spot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.resource.Participant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SpotSystemTest
{
    private SpotSystem spotSystem;
    private HttpClient mockHttpClient;

    private Participant participant;

    private final String httpInterfaceUri = "http://www.example.com";

    @Before
    public void setUp()
    {
        mockHttpClient = mock(HttpClient.class);

        spotSystem = new SpotSystem(httpInterfaceUri);
        spotSystem.setHttpClient(mockHttpClient);

        participant = new Participant();
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
    }

    @Test
    public void test_dropParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", "DROP");
        params.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.dropParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, params);
    }

    @Test
    public void test_dropParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.dropParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }

    @Test
    public void test_muteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", "MUTE");
        params.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.muteParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, params);
    }

    @Test
    public void test_muteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.muteParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }

    @Test
    public void test_unmuteParticipant_invokesPostWithParams() throws SpotCommunicationException, IOException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", "UNMUTE");
        params.put("II_SB_valueToPass", "");

        when(mockHttpClient.getResponseStatus()).thenReturn(200);

        spotSystem.unmuteParticipant(participant);

        verify(mockHttpClient).post(httpInterfaceUri, params);
    }

    @Test
    public void test_unmuteParticipant_whenClientReturnsNon200Status_throwsSpotCommunicationExceptionWithMessage()
        throws IOException
    {
        when(mockHttpClient.getResponseStatus()).thenReturn(400);

        try
        {
            spotSystem.unmuteParticipant(participant);
            fail("Expected SpotCommunicationException for non-200 HTTP status");
        }
        catch(SpotCommunicationException e)
        {
            assertEquals("Received HTTP Status 400 from SPOT System at [" + httpInterfaceUri + "]", e.getMessage());
        }
    }
}
