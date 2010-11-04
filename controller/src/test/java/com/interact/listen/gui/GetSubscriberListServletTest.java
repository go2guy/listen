package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetSubscriberListServletTest extends ListenServletTest
{
    private GetSubscriberListServlet servlet = new GetSubscriberListServlet();

    @Test
    public void test_doGet_withNoCurrentUser_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }
    
    @Test
    public void test_doGet_withNonAdminCurrentUser_throwsUnauthorized() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }
}
