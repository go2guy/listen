package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UnmuteParticipantServletTest extends ListenTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UnmuteParticipantServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new UnmuteParticipantServlet();
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionUnauthorizedStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        request.setMethod("POST");
        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Unauthorized", e.getContent());
        }
    }

    @Test
    public void test_doPost_withNullId_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", (String)null);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide an id", e.getContent());
        }
    }

    @Test
    public void test_doPost_withBlankId_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", " ");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide an id", e.getContent());
        }
    }

    @Test
    public void test_doPost_tryingToUnmuteAdminParticipant_throwsListenServletExceptionWithUnauthorizedStatusWithTextPlainContent()
        throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        conference.setSubscriber(subscriber);

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        subscriber.addToConferences(conference);

        Participant participant = new Participant();
        participant.setAudioResource("/foo/bar");
        participant.setConference(conference);
        participant.setIsAdmin(true);
        participant.setIsAdminMuted(false);
        participant.setIsMuted(false);
        participant.setIsPassive(false);
        participant.setNumber(String.valueOf(System.currentTimeMillis()));
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
        session.save(participant);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Unauthorized - Not allowed to unmute participant", e.getContent());
        }
    }

    @Test
    public void test_doPost_subscriberDoesNotOwnConference_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        Subscriber subscriber = new Subscriber();
        subscriber.setUsername(TestUtil.randomString());
        subscriber.setPassword(TestUtil.randomString());
        session.save(subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        conference.setSubscriber(subscriber);
        session.save(conference);

        Participant participant = new Participant();
        participant.setAudioResource("/foo/bar");
        participant.setConference(conference);
        participant.setIsAdmin(false);
        participant.setIsAdminMuted(false);
        participant.setIsMuted(false);
        participant.setIsPassive(false);
        participant.setNumber(String.valueOf(System.currentTimeMillis()));
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
        session.save(participant);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Unauthorized - Not allowed to unmute participant", e.getContent());
        }
    }

    @Test
    public void test_doPost_subscriberOwnsConferenceAndRequestValid_sendsSpotRequestAndReturns200() throws IOException,
        ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        conference.setSubscriber(subscriber);

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        subscriber.addToConferences(conference);

        Participant participant = new Participant();
        participant.setAudioResource("/foo/bar");
        participant.setConference(conference);
        participant.setIsAdmin(false);
        participant.setIsAdminMuted(false);
        participant.setIsMuted(false);
        participant.setIsPassive(false);
        participant.setNumber(String.valueOf(System.currentTimeMillis()));
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
        session.save(participant);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // TODO assert that the spot system was called
    }

    @Test
    public void test_doPost_subscriberIsAdministratorButDoesNotOwnConferenceAndRequestValid_sendsSpotRequestAndReturns200()
        throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(true);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        Participant participant = new Participant();
        participant.setAudioResource("/foo/bar");
        participant.setConference(conference);
        participant.setIsAdmin(false);
        participant.setIsAdminMuted(false);
        participant.setIsMuted(false);
        participant.setIsPassive(false);
        participant.setNumber(String.valueOf(System.currentTimeMillis()));
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));
        session.save(participant);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // TODO assert that the spot system was called
    }

    @Test(expected = ListenServletException.class)
    public void test_doPost_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("POST");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_UNMUTE_PARTICIPANT);
    }
}
