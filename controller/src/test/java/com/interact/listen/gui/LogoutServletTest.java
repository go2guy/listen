package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.User;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LogoutServletTest
{
    private static final String SESSION_USER_KEY = "user";

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private LogoutServlet servlet = new LogoutServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_removesUserFromSessionAndReturns200() throws IOException, ServletException
    {
        User user = new User();
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_USER_KEY, user);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(session.getAttribute(SESSION_USER_KEY));
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void test_doPost_removesUserFromSessionAndReturns200() throws IOException, ServletException
    {
        // put a user in the session first
        User user = new User();
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_USER_KEY, user);

        request.setMethod("POST");
        servlet.service(request, response);

        assertNull(session.getAttribute(SESSION_USER_KEY));
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
