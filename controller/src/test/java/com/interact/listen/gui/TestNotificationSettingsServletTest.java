package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TestNotificationSettingsServletTest extends ListenTest
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
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber
        
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
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber
        
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
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

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
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber
        
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
}
