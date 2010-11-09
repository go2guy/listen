package com.interact.listen.api;

import com.interact.listen.ListenServletTest;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetAudioFileServletTest extends ListenServletTest
{
    private GetAudioFileServlet servlet = new GetAudioFileServlet();

    @Test
    public void test_doGet_withNullAuthenticatedSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        Authentication auth = Authentication.subscriberAuthentication(null);
        request.setAttribute(AuthenticationFilter.AUTHENTICATION_KEY, auth);
        request.setMethod("GET");

        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }
    
    @Test
    public void test_doGet_withMissingId_throwsBadRequest() throws ServletException, IOException
    {
        Authentication auth = Authentication.subscriberAuthentication(createSubscriber(session));
        request.setAttribute(AuthenticationFilter.AUTHENTICATION_KEY, auth);
        request.setMethod("GET");
        request.setPathInfo("/");
        
        testForListenServletException(servlet, 400, "Please provide an id");
    }
}
