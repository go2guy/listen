package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiResourceLocatorFilterTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ApiResourceLocatorFilter filter;
    private FilterChain mockFilterChain;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        filter = new ApiResourceLocatorFilter();
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    public void test_doFilter_withPathContainingOnlyResource_setsClassKey() throws IOException, ServletException
    {
        request.setPathInfo("/subscribers");
        filter.doFilter(request, response, mockFilterChain);
        assertEquals(Subscriber.class, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
    }

    @Test
    public void test_doFilter_withPathContainingResourceAndId_setsBothKeys() throws IOException, ServletException
    {
        final String id = String.valueOf(System.currentTimeMillis());
        request.setPathInfo("/subscribers/" + id);
        filter.doFilter(request, response, mockFilterChain);
        assertEquals(Subscriber.class, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertEquals(id, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_unrecognizedResource_throwsBadRequestServletException() throws IOException,
        ServletException
    {
        try
        {
            request.setPathInfo("/chickenNuggets");
            filter.doFilter(request, response, mockFilterChain);
            fail("Expected BadRequestServletException for unrecognized resource");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Resource not found for [chickenNuggets]", e.getContent());
        }
    }

    @Test
    public void test_doFilter_unparseableUrl_throwsBadRequestServletException() throws IOException, ServletException
    {
        try
        {
            request.setPathInfo("noStartingSlash");
            filter.doFilter(request, response, mockFilterChain);
            fail("Expected BadRequestServletException for unparseable URL");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Unparseable URL", e.getContent());
        }
    }

    @Test
    public void test_doFilter_nullPathInfo_setsNoAttributes() throws IOException, ServletException
    {
        request.setPathInfo(null);
        filter.doFilter(request, response, mockFilterChain);
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_pathInfoLengthLessThanOne_setsNoAttributes() throws IOException, ServletException
    {
        request.setPathInfo("");
        filter.doFilter(request, response, mockFilterChain);
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_resourceOnlyWithTrailingSlash_throwsBadRequestServletException() throws IOException,
        ServletException
    {
        try
        {
            request.setPathInfo("/subscribers/");
            filter.doFilter(request, response, mockFilterChain);
            fail("Expected BadRequestServletException for unparseable URL");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Unparseable URL", e.getContent());
        }
    }

    @Test
    public void test_doFilter_resourceAndIdWithTrailingSlash_throwsBadRequestServletException() throws IOException,
        ServletException
    {
        try
        {
            request.setPathInfo("/subscribers/1234/");
            filter.doFilter(request, response, mockFilterChain);
            fail("Expected BadRequestServletException for unparseable URL");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Unparseable URL", e.getContent());
        }
    }
}
