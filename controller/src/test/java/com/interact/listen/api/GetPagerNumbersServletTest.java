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
import org.springframework.mock.web.MockHttpServletResponse;

public class GetPagerNumbersServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetPagerNumbersServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new GetPagerNumbersServlet();
    }

    @Test
    public void test_doGet_noAlternateNumber_returnsPagerNumber() throws ServletException, IOException
    {
        final String originalPagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        try
        {
            Configuration.set(Property.Key.PAGER_NUMBER, "123456789");
            request.setMethod("GET");
            servlet.service(request, response);

            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertEquals("text/plain", request.getOutputBufferType());
            assertEquals("123456789", request.getOutputBufferString());
        }
        finally
        {
            Configuration.set(Property.Key.PAGER_NUMBER, originalPagerNumber);
        }
    }
    
    @Test
    public void test_doGet_withAlternateNumber_returnsBothNumbers() throws ServletException, IOException
    {
        final String originalPagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        try
        {
            Configuration.set(Property.Key.PAGER_NUMBER, "123456789");
            Configuration.set(Property.Key.ALTERNATE_NUMBER, "987654321");
            request.setMethod("GET");
            servlet.service(request, response);

            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertEquals("text/plain", request.getOutputBufferType());
            assertEquals("123456789,987654321", request.getOutputBufferString());
        }
        finally
        {
            Configuration.set(Property.Key.PAGER_NUMBER, originalPagerNumber);
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
        }
    }
    
    @Test
    public void test_doGet_noNumbersConfigured_returnsEmptyString() throws ServletException, IOException
    {
        final String originalPagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        try
        {
            Configuration.set(Property.Key.PAGER_NUMBER, "");
            Configuration.set(Property.Key.ALTERNATE_NUMBER, "");
            request.setMethod("GET");
            servlet.service(request, response);

            assertEquals(HttpServletResponse.SC_OK, response.getStatus());
            assertEquals("text/plain", request.getOutputBufferType());
            assertEquals("", request.getOutputBufferString());
        }
        finally
        {
            Configuration.set(Property.Key.PAGER_NUMBER, originalPagerNumber);
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
        }
    }
}
