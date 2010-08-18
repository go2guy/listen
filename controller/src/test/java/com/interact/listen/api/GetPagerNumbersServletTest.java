package com.interact.listen.api;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenServletTest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetPagerNumbersServletTest extends ListenServletTest
{
    private GetPagerNumbersServlet servlet;
    private String originalPagerNumber;
    private String originalAlternatePagerNumber;

    @Before
    public void setUp()
    {
        servlet = new GetPagerNumbersServlet();
        originalPagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        originalAlternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
    }

    @After
    public void tearDown()
    {
        Configuration.set(Property.Key.PAGER_NUMBER, originalPagerNumber);
        Configuration.set(Property.Key.ALTERNATE_NUMBER, originalAlternatePagerNumber);
    }

    @Test
    public void test_doGet_noAlternateNumber_returnsPagerNumber() throws ServletException, IOException
    {
        testSuccessfulRequest("123456789", "", "123456789");
    }

    @Test
    public void test_doGet_withAlternateNumber_returnsBothNumbers() throws ServletException, IOException
    {
        testSuccessfulRequest("123456789", "987654321", "123456789,987654321");
    }

    @Test
    public void test_doGet_noNumbersConfigured_returnsEmptyString() throws ServletException, IOException
    {
        testSuccessfulRequest("", "", "");
    }

    private void testSuccessfulRequest(String pagerNumber, String alternateNumber, String expectedContent)
        throws ServletException, IOException
    {
        Configuration.set(Property.Key.PAGER_NUMBER, pagerNumber);
        Configuration.set(Property.Key.ALTERNATE_NUMBER, alternateNumber);
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertOutputBufferContentEquals(expectedContent);
        assertOutputBufferContentTypeEquals("text/plain");
    }
}
