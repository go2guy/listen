package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceHistoryServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet = new GetConferenceHistoryServlet();

    // TODO do we need to return something different if the conference itself is not found? 404?

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
        assertEquals("Unauthorized", response.getContentAsString());
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
    public void test_doGet_withExistingConferenceAndNoHistory_returnsEmptyList() throws IOException,
        ServletException
    {
        final Long id = System.currentTimeMillis();

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(id));

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setId(System.currentTimeMillis());

        Pin pin = Pin.newInstance(subscriber.getNumber(), PinType.ADMIN);
        session.save(pin);

        conference.addToPins(pin);
        session.save(conference);

        tx.commit();

        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("GET");
        servlet.service(request, response);

        StringBuilder expectedJson = new StringBuilder();
        expectedJson.append("{\"href\":\"/conferenceHistorys?");
        expectedJson.append("_first=0&_max=100&_fields=description,dateCreated,user");
        expectedJson.append("&conference=/conferences/").append(conference.getId()).append("\",");
        expectedJson.append("\"count\":0,");
        expectedJson.append("\"total\":0,");
        expectedJson.append("\"results\":[]}");

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals(expectedJson.toString(), response.getContentAsString());
    }
}
