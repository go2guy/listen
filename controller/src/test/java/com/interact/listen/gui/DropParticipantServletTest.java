package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
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

public class DropParticipantServletTest extends ListenServletTest
{
    private DropParticipantServlet servlet = new DropParticipantServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionUnauthorizedStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        request.setMethod("POST");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withNullId_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", (String)null);
        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an id");
    }

    @Test
    public void test_doPost_withBlankId_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", " ");
        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an id");
    }

    @Test
    public void test_doPost_tryingToDropAdminParticipant_throwsListenServletExceptionWithUnauthorizedStatusWithTextPlainContent()
        throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(TestUtil.randomString());
        subscriber.setUsername(TestUtil.randomString());
        subscriber.setVoicemailPin(TestUtil.randomNumeric(8));
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(TestUtil.randomString());
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

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED,
                                      "Unauthorized - Not allowed to drop participant");
    }

    @Test
    public void test_doPost_subscriberDoesNotOwnConference_throwsListenServletExceptionWithUnauthorized()
        throws IOException, ServletException
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
        session.save(participant); // participant needs an id

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED,
                                      "Unauthorized - Not allowed to drop participant");
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

        verify(statSender).send(Stat.GUI_DROP_PARTICIPANT);
    }
}
