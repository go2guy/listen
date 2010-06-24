package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Pin.PinType;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestNotificationSettingsServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private TestNotificationSettingsServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new TestNotificationSettingsServlet();
        License.setLicense(new AlwaysTrueMockLicense());
    }
    
    @Test
    public void test_doPost_blankMessageType_returnsError()
        throws IOException, ServletException
    {
        setSessionSubscriber(request, true); // admin subscriber
        
        request.setMethod("POST");
        request.setParameter("messageType", "");
        request.setParameter("address", "foo@bar");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide a message type", e.getContent());
        }
    }
    
    @Test
    public void test_doPost_nullMessageType_returnsError()
        throws IOException, ServletException
    {
        setSessionSubscriber(request, true); // admin subscriber
        
        request.setMethod("POST");
        request.setParameter("messageType", (String)null);
        request.setParameter("address", "foo@bar");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide a message type", e.getContent());
        }
    }
    
    @Test
    public void test_doPost_blankAddress_returnsError()
        throws IOException, ServletException
    {
        setSessionSubscriber(request, true); // admin subscriber
        
        request.setMethod("POST");
        request.setParameter("messageType", String.valueOf(System.currentTimeMillis()));
        request.setParameter("address", "");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide an address", e.getContent());
        }
    }
    
    @Test
    public void test_doPost_nullAddress_returnsError()
        throws IOException, ServletException
    {
        setSessionSubscriber(request, true); // admin subscriber
        
        request.setMethod("POST");
        request.setParameter("messageType", String.valueOf(System.currentTimeMillis()));
        request.setParameter("address", (String)null);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide an address", e.getContent());
        }
    }
    
    private void setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(isAdministrator);

        HttpSession session = request.getSession();
        session.setAttribute("subscriber", subscriber);
    }
}
