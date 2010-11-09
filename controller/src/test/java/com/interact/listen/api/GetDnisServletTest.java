package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.ListenServletTest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.ListenServletException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @Test
    public void test_doGet_configurationHasSeveralWildcardsAndQueryStringMatchesAMoreComplexOne_returnsCorrectMapping() throws ServletException, IOException
    {
        testSuccessfulResponse("*:voicemail;4*:conferencing", "4500", "conferencing");
    }

    @Test
    public void test_dnisConfigurationToMap_withValidConfigurationValue_returnsMap() throws ServletException,
        IOException
    {
        final String configString = "foo:bar;baz:quux;1234:5678";

        Map<String, String> result = GetDnisServlet.dnisConfigurationToMap(configString);
        assertTrue(result.containsKey("foo"));
        assertEquals("bar", result.get("foo"));

        assertTrue(result.containsKey("baz"));
        assertEquals("quux", result.get("baz"));

        assertTrue(result.containsKey("1234"));
        assertEquals("5678", result.get("1234"));
    }

    @Test
    public void test_dnisConfigurationToMap_withNullConfigurationValue_returnsEmptyMap() throws ServletException,
        IOException
    {
        assertEquals(0, GetDnisServlet.dnisConfigurationToMap(null).size());
    }

    @Test
    public void test_dnisConfigurationToMap_withBlankConfigurationValue_returnsEmptyMap() throws ServletException,
        IOException
    {
        assertEquals(0, GetDnisServlet.dnisConfigurationToMap("  ").size());
    }

    @Test
    public void test_dnisConfigurationToMap_withConfigValueContainingBlankValue_omitsValueFromMap()
        throws ServletException, IOException
    {
        String config = "foo:bar;;baz:quux";

        Map<String, String> result = GetDnisServlet.dnisConfigurationToMap(config);
        assertEquals(2, result.size());
    }

    @Test
    public void test_dnisConfigurationKeys_withNullConfigurationValue_returnsEmptyList() throws ServletException,
        IOException
    {
        assertEquals(0, GetDnisServlet.dnisConfigurationKeys(null).size());
    }

    @Test
    public void test_dnisConfigurationKeys_withEmptyConfigurationValue_returnsEmptyList() throws ServletException,
        IOException
    {
        assertEquals(0, GetDnisServlet.dnisConfigurationKeys("  ").size());
    }

    @Test
    public void test_dnisConfigurationKeys_withValidConfigurationValue_returnsListWithKeys() throws ServletException,
        IOException
    {
        final String config = "foo:bar;baz:quuux;1234:5678";
        List<String> result = GetDnisServlet.dnisConfigurationKeys(config);

        assertEquals(3, result.size());
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("baz"));
        assertTrue(result.contains("1234"));
    }

    @Test
    public void test_dnisConfigurationKeys_withConfigValueContainingBlankValue_omitsValueFromList()
        throws ServletException, IOException
    {
        String config = "foo:bar;;baz:quux";

        List<String> result = GetDnisServlet.dnisConfigurationKeys(config);
        assertEquals(2, result.size());
    }

    @Test
    public void test_getMappingByType_withNullConfigurationValue_returnsEmptyList() throws ServletException,
        IOException
    {
        String config = null;
        Configuration.set(Property.Key.DNIS_MAPPING, config);

        List<String> result = GetDnisServlet.getMappingByType("doesntmatter");
        assertEquals(0, result.size());
    }

    @Test
    public void test_getMappingByType_withEmptyConfigurationValue_returnsEmptyList() throws ServletException,
        IOException
    {
        String config = " ";
        Configuration.set(Property.Key.DNIS_MAPPING, config);

        List<String> result = GetDnisServlet.getMappingByType("doesntmatter");
        assertEquals(0, result.size());
    }

    @Test
    public void test_getMappingByType_withValidTypeStringThatExistsOnceInConfigurationValue_returnsConfigurationKey()
        throws ServletException, IOException
    {
        String config = "foo:bar;baz:quux;1234:5678";
        Configuration.set(Property.Key.DNIS_MAPPING, config);

        List<String> result = GetDnisServlet.getMappingByType("quux");
        assertEquals(1, result.size());
        assertTrue(result.contains("baz"));
    }

    @Test
    public void test_getMappingByType_withValidTypeStringThatExistsMultipleTimesInConfigurationValue_returnsConfigurationKey()
        throws ServletException, IOException
    {
        String config = "foo:bar;baz:quux;1234:5678;bonk:quux";
        Configuration.set(Property.Key.DNIS_MAPPING, config);

        List<String> result = GetDnisServlet.getMappingByType("quux");
        assertEquals(2, result.size());
        assertTrue(result.contains("baz"));
        assertTrue(result.contains("bonk"));
    }

    @Test
    public void test_getMappingByType_withValidTypeButNotFoundInConfigurationValue_returnsEmptyList()
    {
        String config = "foo:bar;baz:quux;1234:5678";
        Configuration.set(Property.Key.DNIS_MAPPING, config);

        List<String> result = GetDnisServlet.getMappingByType("random");
        assertEquals(0, result.size());
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
