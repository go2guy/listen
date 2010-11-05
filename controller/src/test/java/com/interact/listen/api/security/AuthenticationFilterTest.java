package com.interact.listen.api.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationFilterTest extends ListenServletTest
{
    private FilterChain mockFilterChain;
    private AuthenticationFilter filter = new AuthenticationFilter();

    @Before
    public void setUp()
    {
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    public void test_doFilter_whenAuthenticationIsDisabled_casuallyExplainsTheresNothingToSeeHereAndDirectsTheUserToMoveAlong()
        throws ServletException, IOException
    {
        String original = Configuration.get(Property.Key.AUTHENTICATE_API);
        Configuration.set(Property.Key.AUTHENTICATE_API, "false");

        try
        {
            filter.doFilter(request, response, mockFilterChain);
            verify(mockFilterChain).doFilter(request, response);
        }
        finally
        {
            Configuration.set(Property.Key.AUTHENTICATE_API, original);
        }
    }
}
