package com.interact.listen.gui;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetPropertiesServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetPropertiesServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new GetPropertiesServlet();
    }

    @Test
    public void test_doGet_withNoSessionUser_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("user") == null;

        request.setMethod("GET");
        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("Unauthorized", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doGet_withNonAdministratorSessionUser_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        setSessionUser(request, false);
        request.setMethod("GET");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("Unauthorized", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doGet_withValidPermissions_returnsJsonObjectWithCorrectContentType() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json", request.getOutputBufferType());
        assertTrue(request.getOutputBufferString().startsWith("{"));
        assertTrue(request.getOutputBufferString().endsWith("}"));
    }

    // TODO this is used in several servlets - refactor it into some test utility class
    private void setSessionUser(HttpServletRequest request, boolean isAdmin)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setIsAdministrator(isAdmin);
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }
}
