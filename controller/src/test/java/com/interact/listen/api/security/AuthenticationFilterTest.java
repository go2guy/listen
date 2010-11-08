package com.interact.listen.api.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.api.util.HttpDate;
import com.interact.listen.api.util.Signature;
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

    @Test
    public void test_doFilter_withUnrecognizedAuthenticationType_throwsUnauthorized() throws ServletException,
        IOException
    {
        request.addHeader("X-Listen-AuthenticationType", "QkxBUkdIIQ=="); // = "BLARGH!"

        testForListenServletException(filter, request, response, mockFilterChain, 401,
                                      "Unauthorized - Authentication has unknown type [BLARGH!]");
    }

    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndMissingDateHeader_throwsUnauthorized()
        throws ServletException, IOException
    {
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("X-Listen-Signature", "1234"); // doesn't matter, should hit date validation first

        assert request.getHeader("Date") == null;

        testForListenServletException(filter, request, response, mockFilterChain, 401,
                                      "Unauthorized - Missing authorization component(s)");
    }

    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndMissingSignatureHeader_throwsUnauthorized()
        throws ServletException, IOException
    {
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("X-Listen-Date", HttpDate.now());

        assert request.getHeader("X-Listen-Signature") == null;

        testForListenServletException(filter, request, response, mockFilterChain, 401,
                                      "Unauthorized - Missing authorization component(s)");
    }

    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndInvalidSignatureForDate_throwsUnauthorized()
        throws ServletException, IOException
    {
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("Date", HttpDate.now());
        request.addHeader("X-Listen-Signature", "QkxBUkdIIQ=="); // BLARGH!

        testForListenServletException(filter, request, response, mockFilterChain, 401,
                                      "Unauthorized - Signature is invalid");
    }

    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndInvalidDateFormat_throwsUnauthorized()
        throws ServletException, IOException
    {
        String date = "Invalid format";
        
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("Date", date);
        request.addHeader("X-Listen-Signature", Signature.create(date));

        testForListenServletException(filter, request, response, mockFilterChain, 400,
                                      "Date header is not properly formatted");
    }
    
    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndExpiredRequest_throwsUnauthorized() throws ServletException, IOException
    {
        String date = "Mon, 01 Nov 2010 10:00:00 CST"; // hopefully this is > 5 minutes ago...
        
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("Date", date);
        request.addHeader("X-Listen-Signature", Signature.create(date));
        
        testForListenServletException(filter, request, response, mockFilterChain, 401, "Unauthorized - Request expired");
    }

    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndRequestFarInTheFuture_throwsUnauthorized() throws ServletException, IOException
    {
        String date = "Mon, 01 Jan 2034 10:00:00 CST"; // hopefully this is > 5 minutes from now...
        
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("Date", date);
        request.addHeader("X-Listen-Signature", Signature.create(date));
        
        testForListenServletException(filter, request, response, mockFilterChain, 401, "Unauthorized - Request expired");
    }
    
    @Test
    public void test_doFilter_withSystemAuthenticationTypeAndValidAuthenticationAttributes_invokesDoFilter() throws ServletException, IOException
    {
        String date = HttpDate.now();
        
        request.addHeader("X-Listen-AuthenticationType", "U1lTVEVN"); // SYSTEM
        request.addHeader("Date", date);
        request.addHeader("X-Listen-Signature", Signature.create(date));
        
        filter.doFilter(request, response, mockFilterChain);
        verify(mockFilterChain).doFilter(request, response);
    }
}
