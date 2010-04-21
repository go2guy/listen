package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UnmuteParticipantServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new UnmuteParticipantServlet();
    }

    @Test
    public void test_doPost_withNoSessionUser_returnsUnauthorizedStatusAndTextPlainContent() throws IOException,
        ServletException
    {
        request.setMethod("POST");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Unauthorized", response.getContentAsString());
    }

    @Test
    public void test_doPost_withNullId_returnsBadRequestStatusAndTextPlainContent() throws IOException,
        ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", (String)null);
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Please provide an id", response.getContentAsString());
    }

    @Test
    public void test_doPost_withBlankId_returnsBadRequestStatusAndTextPlainContent() throws IOException,
        ServletException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", " ");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Please provide an id", response.getContentAsString());
    }

    @Test
    public void test_doPost_tryingToUnmuteAdminParticipant_returnsUnauthorizedStatusWithTextPlainContent()
        throws IOException, ServletException
    {
        setSessionUser(request);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setId(System.currentTimeMillis());

        Pin pin = Pin.newInstance(String.valueOf(System.currentTimeMillis()), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

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

        tx.commit();

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(participant.getId()));
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Not allowed to unmute participant", response.getContentAsString());
    }

    private void setSessionUser(HttpServletRequest request)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }
}
