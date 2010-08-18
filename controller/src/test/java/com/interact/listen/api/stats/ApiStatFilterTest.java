package com.interact.listen.api.stats;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiStatFilterTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ApiStatFilter filter;

    private StatSender mockStatSender;
    private FilterChain mockFilterChain;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();

        mockStatSender = mock(StatSender.class);
        mockFilterChain = mock(FilterChain.class);

        filter = new ApiStatFilter();
        filter.setStatSender(mockStatSender);
    }

    @Test
    public void test_doFilter_withConferenceResourceAndGetMethod_invokesSendWithCorrectStatAndInvokesFilterChain()
        throws IOException, ServletException
    {
        performDoFilterAndVerify("/conferences");
        verify(mockStatSender).send(Stat.API_CONFERENCE_GET);
    }

    @Test
    public void test_doFilter_withUnrecognizedResource_doesNotInvokeSendAndThrowsNoExceptionAndInvokesFilterChain()
        throws IOException, ServletException
    {
        // this method shouldn't throw an Exception :)
        performDoFilterAndVerify("/mandarinOranges");
        verifyZeroInteractions(mockStatSender);
    }

    @Test
    public void test_doFilter_withNullResource_doesNotInvokeSendAndThrowsNoexceptionAndInvokesFilterChain()
        throws IOException, ServletException
    {
        performDoFilterAndVerify(null); // null pathInfo will yield null resource
        verifyZeroInteractions(mockStatSender);
    }

    private void performDoFilterAndVerify(String pathInfo) throws IOException, ServletException
    {
        request.setPathInfo(pathInfo);
        request.setMethod("GET");
        filter.doFilter(request, response, mockFilterChain);
        verify(mockFilterChain).doFilter(request, response);
    }
}
