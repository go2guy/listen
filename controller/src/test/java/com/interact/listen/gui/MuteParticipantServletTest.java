package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.*;
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

public class MuteParticipantServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MuteParticipantServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new MuteParticipantServlet();
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
    public void test_doPost_tryingToMuteAdminParticipant_returnsUnauthorizedStatusWithTextPlainContent()
        throws IOException, ServletException
    {
        setSessionUser(request);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
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
        assertEquals("Not allowed to mute participant", response.getContentAsString());
    }

    @Test
    public void test_doPost_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("POST");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_MUTE_PARTICIPANT);
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
