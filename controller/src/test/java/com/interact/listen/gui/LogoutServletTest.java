package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

public class LogoutServletTest extends ListenServletTest
{
    private static final String SESSION_SUBSCRIBER_KEY = "subscriber";
    private LogoutServlet servlet = new LogoutServlet();

    @Test
    public void test_doGet_removesSubscriberFromSessionAndReturns200() throws IOException, ServletException
    {
        Subscriber subscriber = new Subscriber();
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(SESSION_SUBSCRIBER_KEY, subscriber);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(httpSession.getAttribute(SESSION_SUBSCRIBER_KEY));
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void test_doPost_removesSubscriberFromSessionAndReturns200() throws IOException, ServletException
    {
        // put a subscriber in the session first
        Subscriber subscriber = new Subscriber();
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute(SESSION_SUBSCRIBER_KEY, subscriber);

        request.setMethod("POST");
        servlet.service(request, response);

        assertNull(httpSession.getAttribute(SESSION_SUBSCRIBER_KEY));
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void test_doPost_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("POST");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_LOGOUT);
    }
}
