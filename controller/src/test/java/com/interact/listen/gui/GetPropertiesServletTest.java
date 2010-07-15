package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetPropertiesServletTest extends ListenTest
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
    public void test_doGet_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

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
    public void test_doGet_withNonAdministratorSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
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
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/json", request.getOutputBufferType());
        assertTrue(request.getOutputBufferString().startsWith("{"));
        assertTrue(request.getOutputBufferString().endsWith("}"));
    }
}
