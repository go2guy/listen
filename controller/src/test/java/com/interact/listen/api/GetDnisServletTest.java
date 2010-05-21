package com.interact.listen.api;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetDnisServletTest
{
    private MockHttpServletRequest request;
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
    public void test_doGet_nullNumber_returnsBadRequest() throws ServletException, IOException
    {
        request.setMethod("GET");
        request.setParameter("number", (String)null);
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Please provide a number", response.getContentAsString());
    }

    @Test
    public void test_doGet_blankNumber_returnsBadRequest() throws ServletException, IOException
    {
        request.setMethod("GET");
        request.setParameter("number", " ");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("Please provide a number", response.getContentAsString());
    }

    @Test
    public void test_doGet_numberNotFound_returns404NotFound() throws ServletException, IOException
    {
        final String originalDnisValue = Configuration.get(Property.Key.DNIS_MAPPING);
        try
        {
            Configuration.set(Property.Key.DNIS_MAPPING, "");
            request.setMethod("GET");
            request.setParameter("number", "1234");
            servlet.service(request, response);

            assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
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
            assertEquals("text/plain", response.getContentType());
            assertEquals("conferencing", response.getContentAsString());
        }
        finally
        {
            Configuration.set(Property.Key.DNIS_MAPPING, originalDnisValue);
        }
    }
}
