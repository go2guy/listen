package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.*;

import org.junit.Before;
import org.junit.Test;

public class OutputBufferFilterTest extends ListenServletTest
{
    private OutputBufferFilter filter = new OutputBufferFilter();
    private FilterChain mockFilterChain;

    @Before
    public void setUp()
    {
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    public void test_doFilter_withRequestThatDoesntPopulateBuffer_returnsDefaultBufferValues() throws ServletException,
        IOException
    {
        filter.doFilter(request, response, mockFilterChain);

        verify(mockFilterChain).doFilter(request, response);

        assertEquals(0, ((StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY)).length());
        assertEquals("text/plain", request.getAttribute(OutputBufferFilter.OUTPUT_TYPE_KEY));
        assertEquals(Boolean.FALSE, request.getAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY));
    }

    @Test
    public void test_doFilter_withRequestThatSuppressesOutput_suppressesOutput() throws ServletException, IOException
    {
        filter.doFilter(request, response, new OutputSuppressingFilterChain());
        assertEquals(0, response.getContentLength());
    }

    @Test
    public void test_doFilter_withContent_setsResponseContent() throws ServletException, IOException
    {
        String content = TestUtil.randomString();
        String contentType = TestUtil.randomString();

        filter.doFilter(request, response, new FilterChainThatAddsStuffToOutputBuffer(content, contentType));

        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertEquals(content.length(), response.getContentLength());
        assertEquals(contentType, response.getContentType());
        assertEquals(content, response.getContentAsString());
    }

    private static class OutputSuppressingFilterChain implements FilterChain
    {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response)
        {
            request.setAttribute(OutputBufferFilter.OUTPUT_SUPPRESS_KEY, Boolean.TRUE);
        }
    }

    private static class FilterChainThatAddsStuffToOutputBuffer implements FilterChain
    {
        private String content;
        private String contentType;

        public FilterChainThatAddsStuffToOutputBuffer(String content, String contentType)
        {
            this.content = content;
            this.contentType = contentType;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response)
        {
            request.setAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY, new StringBuilder(content));
            request.setAttribute(OutputBufferFilter.OUTPUT_TYPE_KEY, contentType);
        }
    }
}
