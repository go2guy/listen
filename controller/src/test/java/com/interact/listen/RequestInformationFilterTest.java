package com.interact.listen;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Test;

public class RequestInformationFilterTest extends ListenServletTest
{
    @Test
    public void test_doFilter_executesFilterChainDoFilter() throws ServletException, IOException
    {
        FilterChain chain = mock(FilterChain.class);
        RequestInformationFilter filter = new RequestInformationFilter();

        request.addHeader("X-Listen-Channel", "TUI");
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
