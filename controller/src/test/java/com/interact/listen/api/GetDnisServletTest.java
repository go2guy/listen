package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.ListenServletTest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GetDnisServletTest extends ListenServletTest
{
    private GetDnisServlet servlet;
    private String originalDnisConfiguration;

    @Before
    public void setUp()
    {
        servlet = new GetDnisServlet();
        originalDnisConfiguration = Configuration.get(Property.Key.DNIS_MAPPING);
    }

    @After
    public void tearDown()
    {
        Configuration.set(Property.Key.DNIS_MAPPING, originalDnisConfiguration);
    }

    @Test
    public void test_doGet_nullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException, IOException
    {
        testExpectedListenServletException("", null, HttpServletResponse.SC_BAD_REQUEST, "Please provide a number",
                                           "text/plain");
    }

    @Test
    public void test_doGet_blankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        testExpectedListenServletException("", " ", HttpServletResponse.SC_BAD_REQUEST, "Please provide a number",
                                           "text/plain");
    }

    @Test
    public void test_doGet_numberNotFound_throwsListenServletExceptionWith404NotFound() throws ServletException,
        IOException
    {
        testExpectedListenServletException("", "1234", HttpServletResponse.SC_NOT_FOUND, "", "");
    }

    @Test
    public void test_doGet_numberFound_returnsMappedValue() throws ServletException, IOException
    {
        testSuccessfulResponse("1234:voicemail;1800AWESOME:conferencing;4242:mailbox", "1800AWESOME", "conferencing");
    }

    @Test
    public void test_doGet_wildcardFirstButLookingForLaterNumber_returnsLaterNumber() throws ServletException,
        IOException
    {
        testSuccessfulResponse("*:voicemail;1800AWESOME:conferencing;4242:conferencing", "4242", "conferencing");
    }

    @Test
    public void test_doGet_configurationHasWildcardAndNotQueryString_returnsWildcardMapping() throws ServletException,
        IOException
    {
        testSuccessfulResponse("*:voicemail;1800AWESOME:conferencing;4242:conferencing", "9999", "voicemail");
    }

    private void setDnisAndPerformRequest(String dnisMappings, String number) throws IOException, ServletException
    {
        Configuration.set(Property.Key.DNIS_MAPPING, dnisMappings);
        request.setMethod("GET");
        request.setParameter("number", number);
        servlet.service(request, response);
    }

    private void testSuccessfulResponse(String dnis, String number, String expectedContent) throws IOException,
        ServletException
    {
        setDnisAndPerformRequest(dnis, number);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertOutputBufferContentEquals(expectedContent);
        assertOutputBufferContentTypeEquals("text/plain");
    }

    private void testExpectedListenServletException(String dnis, String number, int expectedStatus,
                                                    String expectedContent, String expectedContentType)
        throws IOException, ServletException
    {
        try
        {
            setDnisAndPerformRequest(dnis, number);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(expectedStatus, e.getStatus());
            assertEquals(expectedContent, e.getContent());
            assertEquals(expectedContentType, e.getContentType());
        }
    }
}
