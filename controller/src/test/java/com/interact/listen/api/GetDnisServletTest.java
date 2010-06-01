package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetDnisServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetDnisServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new GetDnisServlet();
    }

    @Test
    public void test_doGet_nullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException, IOException
    {
        request.setMethod("GET");
        request.setParameter("number", (String)null);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide a number", e.getContent());
        }
    }

    @Test
    public void test_doGet_blankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        request.setMethod("GET");
        request.setParameter("number", " ");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals("Please provide a number", e.getContent());
        }
    }

    @Test
    public void test_doGet_numberNotFound_throwsListenServletExceptionWith404NotFound() throws ServletException,
        IOException
    {
        final String originalDnisValue = Configuration.get(Property.Key.DNIS_MAPPING);
        try
        {
            Configuration.set(Property.Key.DNIS_MAPPING, "");
            request.setMethod("GET");
            request.setParameter("number", "1234");
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.getStatus());
        }
        finally
        {
            Configuration.set(Property.Key.DNIS_MAPPING, originalDnisValue);
        }
    }

    @Test
    public void test_doGet_numberFound_returnsMappedValue() throws ServletException, IOException
    {
        final String originalDnisValue = Configuration.get(Property.Key.DNIS_MAPPING);
        try
        {
            Configuration.set(Property.Key.DNIS_MAPPING, "1234:voicemail;1800AWESOME:conferencing;4242:mailbox");
            request.setMethod("GET");
            request.setParameter("number", "1800AWESOME");
            servlet.service(request, response);

            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertEquals("text/plain", request.getOutputBufferType());
            assertEquals("conferencing", request.getOutputBufferString());
        }
        finally
        {
            Configuration.set(Property.Key.DNIS_MAPPING, originalDnisValue);
        }
    }
}
