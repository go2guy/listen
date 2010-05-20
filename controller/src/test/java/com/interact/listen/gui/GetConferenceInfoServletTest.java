package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceInfoServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetConferenceInfoServlet servlet = new GetConferenceInfoServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_withNoSessionUser_returnsUnauthorized() throws IOException, ServletException
    {
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized - not logged in", response.getContentAsString());
    }

    @Test
    public void test_doGet_withNonexistentConference_returns500() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(id));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Conference not found", response.getContentAsString());
    }

    @Test
    public void test_doGet_withExistingConference_returns200AndConferenceJSON() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(id));
        session.save(subscriber);

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        session.save(conference);

        User user = new User();
        user.setSubscriber(subscriber);
        user.setUsername(String.valueOf(System.currentTimeMillis()));
        user.setPassword(String.valueOf(System.currentTimeMillis()));
        user.addToConferences(conference);
        session.save(user);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("GET");
        servlet.service(request, response);

        tx.commit();

        String hrefString = "\"href\":\"/conferences/" + conference.getId() + "\"";
        assertTrue(response.getContentAsString().contains(hrefString));
    }

    @Test
    public void test_doGet_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("GET");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_GET_CONFERENCE_INFO);
    }
}
