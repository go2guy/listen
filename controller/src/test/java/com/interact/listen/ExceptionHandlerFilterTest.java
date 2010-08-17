package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class ExceptionHandlerFilterTest extends ListenServletTest
{
    private Filter filter = new ExceptionHandlerFilter();

    @Test
    public void test_doFilter_withListenServletException_appendsOutputBufferContent() throws ServletException,
        IOException
    {
        ListenServletException e = new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                                              TestUtil.randomString(), "text/plain");
        performRequest(e);

        assertOutputBufferContentEquals(e.getContent());
        assertOutputBufferContentTypeEquals(e.getContentType());
        assertEquals(e.getStatus(), response.getStatus());
    }

    @Test
    public void test_doFilter_withExceptionCausedByListenServletException_appendsOutputBufferContent()
        throws ServletException, IOException
    {
        ListenServletException cause = new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                                                  TestUtil.randomString(), "text/plain");
        RuntimeException e = new RuntimeException(cause);
        performRequest(e);

        assertOutputBufferContentEquals(cause.getContent());
        assertOutputBufferContentTypeEquals(cause.getContentType());
        assertEquals(cause.getStatus(), response.getStatus());
    }

    @Test
    public void test_doFilter_withNonListenServletException_appendsOutputBufferContent() throws ServletException,
        IOException
    {
        performRequest(new RuntimeException());
        
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertOutputBufferContentEquals("An unknown error occurred, please contact the system administrator");
        assertOutputBufferContentTypeEquals("text/plain");
    }

    private void performRequest(Exception exceptionToThrow) throws ServletException, IOException
    {
        FilterChain filterChain = mock(FilterChain.class);
        doThrow(exceptionToThrow).when(filterChain).doFilter(request, response);
        filter.doFilter(request, response, filterChain);
    }
}
