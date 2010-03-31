package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ServletUtilTest
{
    private MockHttpServletRequest request;

    @Before
    public void setUp()
    {
        request = new MockHttpServletRequest();
    }

    @Test
    public void test_getQueryParameters_withNullQueryString_returnsEmptyMap()
    {
        request.setQueryString(null);
        Map<String, String> params = ServletUtil.getQueryParameters(request);
        assertEquals(0, params.size());
    }

    @Test
    public void test_getQueryParameters_withBlankQueryString_returnsEmptyMap()
    {
        request.setQueryString("");
        Map<String, String> params = ServletUtil.getQueryParameters(request);
        assertEquals(0, params.size());
    }

    @Test
    public void test_getQueryParameters_withOneParameter_returnsCorrectMap()
    {
        request.setQueryString("foo=bar");
        Map<String, String> params = ServletUtil.getQueryParameters(request);

        assertEquals(1, params.size());
        assertEquals("bar", params.get("foo"));
    }

    @Test
    public void test_getQueryParameters_withTwoParameters_returnsCorrectMap()
    {
        request.setQueryString("red=blue&yellow=green");
        Map<String, String> params = ServletUtil.getQueryParameters(request);

        assertEquals(2, params.size());
        assertEquals("blue", params.get("red"));
        assertEquals("green", params.get("yellow"));
    }

    @Test
    public void test_getQueryParameters_withParameterWithNoAssignment_skipsParameter()
    {
        request.setQueryString("red&yellow=green");
        Map<String, String> params = ServletUtil.getQueryParameters(request);

        assertEquals(1, params.size());
        assertEquals("green", params.get("yellow"));
        assertFalse(params.containsKey("red"));
    }
}
