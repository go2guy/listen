package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class StopRecordingServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private StopRecordingServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new StopRecordingServlet();
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
        setSessionSubscriber(request, false);
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
        setSessionSubscriber(request, false);
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
    public void test_doPost_subscriberDoesNotOwnConference_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        setSessionSubscriber(request, false);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        session.save(conference);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Unauthorized - Not allowed to stop recording", e.getContent());
        }
        finally
        {
            tx.rollback();
        }
    }
    
    @Test
    public void test_doPost_conferenceNotStarted_throwsListenServletExceptionWithBadRequestStatusAndTextPlainContent() throws IOException,
        ServletException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);

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

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Conference must be started for recording", e.getContent());
        }
        finally
        {
            tx.rollback();
        }
    }

    //@Test
    public void test_doPost_subscriberOwnsConferenceAndRequestValid_sendsSpotRequestAndReturns200() throws IOException,
        ServletException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
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

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));
        servlet.service(request, response);

        tx.rollback();

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // TODO assert that the spot system was called
    }

    //@Test
    public void test_doPost_subscriberIsAdministratorButDoesNotOwnConferenceAndRequestValid_sendsSpotRequestAndReturns200()
        throws IOException, ServletException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setIsAdministrator(true);
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

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(conference.getId()));
        servlet.service(request, response);

        tx.rollback();

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

        verify(statSender).send(Stat.GUI_STOP_RECORDING);
    }

    private void setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(isAdministrator);

        HttpSession session = request.getSession();
        session.setAttribute("subscriber", subscriber);
    }
}
