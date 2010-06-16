package com.interact.listen.api.license;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.api.ApiResourceLocatorFilter;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiLicenseFilterTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ApiLicenseFilter filter;

    private FilterChain mockFilterChain;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();

        mockFilterChain = mock(FilterChain.class);

        filter = new ApiLicenseFilter();
    }

    @Test
    public void test_doFilter_accessingVoicemailWhenVoicemailIsUnlicensed_throwsNotLicensedException()
        throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Voicemail.class);

        try
        {
            filter.doFilter(request, response, mockFilterChain);
        }
        catch(NotLicensedException e)
        {
            assertEquals("Feature VOICEMAIL is not licensed", e.getContent());
            assertEquals(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getStatus());
        }
    }

    @Test
    public void test_doFilter_accessingFeatureThatIsNotLicensable_invokesFilterChainDoFilter() throws IOException,
        ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        filter.doFilter(request, response, mockFilterChain);
        verify(mockFilterChain).doFilter(request, response);
    }
}
