package com.interact.listen.stats;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.interact.listen.InputStreamMockHttpServletRequest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class StatFilterTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private StatFilter filter;

    private StatSender mockStatSender;
    private FilterChain mockFilterChain;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();

        mockStatSender = mock(StatSender.class);
        mockFilterChain = mock(FilterChain.class);

        filter = new StatFilter();
        filter.setStatSender(mockStatSender);
    }

    @Test
    public void test_doFilter_withConferenceResourceAndGetMethod_invokesSendWithCorrectStatAndInvokesFilterChain()
        throws IOException, ServletException
    {
        request.setPathInfo("/conferences");
        request.setMethod("GET");

        filter.doFilter(request, response, mockFilterChain);

        verify(mockStatSender).send(Stat.API_CONFERENCE_GET);
        verify(mockFilterChain).doFilter(request, response);
    }

    @Test
    public void test_doFilter_withUnrecognizedResource_doesNotInvokeSendAndThrowsNoExceptionAndInvokesFilterChain()
        throws IOException, ServletException
    {
        // this method shouldn't throw an Exception :)

        request.setPathInfo("/mandarinOranges");
        request.setMethod("GET");

        filter.doFilter(request, response, mockFilterChain);

        verifyZeroInteractions(mockStatSender);
        verify(mockFilterChain).doFilter(request, response);
    }
}
