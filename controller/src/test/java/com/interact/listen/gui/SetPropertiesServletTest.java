package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SetPropertiesServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private SetPropertiesServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new SetPropertiesServlet();
    }

    @Test
    public void test_doPost_withNoSessionUser_returnsUnauthorized() throws ServletException, IOException
    {
        assert request.getSession().getAttribute("user") == null;

        request.setMethod("POST");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    public void test_doPost_withNonAdministratorSessionUser_returnsUnauthorized() throws ServletException, IOException
    {
        setSessionUser(request, false);

        request.setMethod("POST");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized", response.getContentAsString());
        assertEquals("text/plain", response.getContentType());
    }

    @Test
    public void test_doPost_withProperty_setsProperty() throws ServletException, IOException
    {
        setSessionUser(request, true);

        final String value = String.valueOf(System.currentTimeMillis());

        request.setMethod("POST");
        request.setParameter(Property.Key.DNIS_MAPPING.getKey(), value);
        servlet.service(request, response);

        assertEquals(value, Configuration.get(Property.Key.DNIS_MAPPING));
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
