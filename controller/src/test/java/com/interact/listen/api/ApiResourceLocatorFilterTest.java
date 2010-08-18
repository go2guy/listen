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
        performDoFilter("/subscribers");
        assertEquals(Subscriber.class, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
    }

    @Test
    public void test_doFilter_withPathContainingResourceAndId_setsBothKeys() throws IOException, ServletException
    {
        final String id = String.valueOf(System.currentTimeMillis());
        performDoFilter("/subscribers/" + id);
        assertEquals(Subscriber.class, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertEquals(id, request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_unrecognizedResource_throwsBadRequestServletException() throws IOException,
        ServletException
    {
        try
        {
            performDoFilter("/chickenNuggets");
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
            performDoFilter("noStartingSlash");
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
        performDoFilter(null);
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_pathInfoLengthLessThanOne_setsNoAttributes() throws IOException, ServletException
    {
        performDoFilter("");
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY));
        assertNull(request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY));
    }

    @Test
    public void test_doFilter_resourceOnlyWithTrailingSlash_throwsBadRequestServletException() throws IOException,
        ServletException
    {
        try
        {
            performDoFilter("/subscribers/");
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
            performDoFilter("/subscribers/1234/");
            fail("Expected BadRequestServletException for unparseable URL");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Unparseable URL", e.getContent());
        }
    }

    private void performDoFilter(String pathInfo) throws IOException, ServletException
    {
        request.setPathInfo(pathInfo);
        filter.doFilter(request, response, mockFilterChain);
    }
}
