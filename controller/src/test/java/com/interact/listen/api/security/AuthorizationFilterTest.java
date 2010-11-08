package com.interact.listen.api.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.UnauthorizedServletException;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AuthorizationFilterTest extends ListenServletTest
{
    private FilterChain mockFilterChain;
    private AuthorizationFilter filter = new AuthorizationFilter();
    private String original;

    @Before
    public void setUp()
    {
        mockFilterChain = mock(FilterChain.class);
        original = Configuration.get(Property.Key.AUTHENTICATE_API);

        // most tests require it enabled, so just set it to true
        Configuration.set(Property.Key.AUTHENTICATE_API, "true");
    }

    @After
    public void tearDown()
    {
        Configuration.set(Property.Key.AUTHENTICATE_API, original);
    }

    @Test
    public void test_doFilter_whenAuthenticationIsDisabled_callsDoFilter() throws ServletException, IOException
    {
        Configuration.set(Property.Key.AUTHENTICATE_API, "false");

        filter.doFilter(request, response, mockFilterChain);
        verify(mockFilterChain).doFilter(request, response);

    }

    @Test
    public void test_doFilter_withNoAuthenticationRequestAttribute_throwsUnauthorized() throws ServletException,
        IOException
    {
        request.setAttribute(AuthenticationFilter.AUTHENTICATION_KEY, null);

        try
        {
            filter.doFilter(request, response, mockFilterChain);
            fail("Expected UnauthorizedServletException");
        }
        catch(UnauthorizedServletException e)
        {
            assertEquals(401, e.getStatus());
            assertEquals("Unauthorized - Not authenticated", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }

    @Test
    public void test_doFilter_withSystemAuthenticationType_invokesDoFilter() throws ServletException, IOException
    {
        Authentication auth = Authentication.systemAuthentication("remote");
        request.setAttribute(AuthenticationFilter.AUTHENTICATION_KEY, auth);

        filter.doFilter(request, response, mockFilterChain);
        verify(mockFilterChain).doFilter(request, response);
    }
}
