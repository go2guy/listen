package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class SetPropertiesServletTest extends ListenTest
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
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");

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
    public void test_doPost_withNonAdministratorSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");

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
    public void test_doPost_withProperty_setsProperty() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        final String value = String.valueOf(System.currentTimeMillis());

        request.setMethod("POST");
        request.setParameter(Property.Key.MAIL_FROMADDRESS.getKey(), value);
        servlet.service(request, response);

        assertEquals(value, Configuration.get(Property.Key.MAIL_FROMADDRESS));
    }
}
