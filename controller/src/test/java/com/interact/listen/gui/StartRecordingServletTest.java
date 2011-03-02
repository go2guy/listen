package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class StartRecordingServletTest extends ListenServletTest
{
    private StartRecordingServlet servlet = new StartRecordingServlet();

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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Id cannot be null");
    }

    @Test
    public void test_doPost_withBlankId_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("id", " ");

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Id must be a number");
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

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED,
                                      "Unauthorized - Not allowed to start recording");
    }

    @Test
    public void test_doPost_conferenceNotStarted_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent()
        throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber.getId());

        Conference conference = new Conference();
        conference.setIsStarted(false);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        conference.setSubscriber(subscriber);

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        subscriber.addToConferences(conference);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST,
                                      "Conference must be started for recording");
    }

    // @Test
    public void test_doPost_subscriberOwnsConferenceAndRequestValid_sendsSpotRequestAndReturns200() throws IOException,
        ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber.getId());

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

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // TODO assert that the spot system was called
    }

    // @Test
    public void test_doPost_subscriberIsAdministratorButDoesNotOwnConferenceAndRequestValid_sendsSpotRequestAndReturns200()
        throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(true);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber.getId());

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));
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

        verify(statSender).send(Stat.GUI_START_RECORDING);
    }
}
