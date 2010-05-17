package com.interact.listen;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        Constructor<?> constructor = ServletUtil.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        try
        {
            constructor.newInstance();
            fail("Expected InvocationTargetException with root cause of AssertionError for utility class constructor");
        }
        catch(InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AssertionError);
            assertEquals("Cannot instantiate utility class ServletUtil", cause.getMessage());
        }
    }
}
