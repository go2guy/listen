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
import org.springframework.mock.web.MockHttpServletResponse;

public class OutdialServletTest extends ListenTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private OutdialServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new OutdialServlet();
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", TestUtil.randomString() + "foo");
        
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
    public void test_doPost_withNullConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", (String)null);
        request.setParameter("number", TestUtil.randomString() + "foo");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a conferenceId", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doPost_withBlankConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", " ");
        request.setParameter("number", TestUtil.randomString() + "foo");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a conferenceId", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doPost_withNullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", (String)null);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doPost_withBlankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", " ");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doPost_withConferenceNotFound_throwsListenServletExceptionWithBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", String.valueOf(TestUtil.randomNumeric(9))); // hopefully doesn't exist
        request.setParameter("number", TestUtil.randomString() + "foo");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Conference not found", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }
}
