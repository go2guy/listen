package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.exception.ListenRuntimeException;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.hibernate.StaleObjectStateException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class OpenSessionInViewFilterTest
{
    protected InputStreamMockHttpServletRequest request;
    protected MockHttpServletResponse response;

    Filter filter;
    FilterChain chain;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();

        filter = new OpenSessionInViewFilter();

        chain = mock(FilterChain.class);
    }

    @Test
    public void test_doFilter_whenDoFilterThrowsException_throwsServletExceptionWithCause() throws ServletException,
        IOException
    {
        filter.init(null);
        Exception npe = new ListenRuntimeException(null);
        doThrow(npe).when(chain).doFilter(request, response);

        try
        {
            filter.doFilter(request, response, chain);
        }
        catch(ServletException e)
        {
            verify(chain).doFilter(request, response);
            assertEquals(npe, e.getCause());
        }
    }

    @Test
    public void test_doFilter_whenFilterThrowsStaleObjectStateException_rethrowsSOSE() throws ServletException,
        IOException
    {
        filter.init(null);
        StaleObjectStateException sose = new StaleObjectStateException(null, null);
        doThrow(sose).when(chain).doFilter(request, response);

        try
        {
            filter.doFilter(request, response, chain);
        }
        catch(StaleObjectStateException e)
        {
            verify(chain).doFilter(request, response);
            assertEquals(sose, e);
        }
    }

    @Test
    public void test_doFilter_whenFilterThrowsNoException_doesntThrowException() throws ServletException, IOException
    {
        filter.init(null);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
