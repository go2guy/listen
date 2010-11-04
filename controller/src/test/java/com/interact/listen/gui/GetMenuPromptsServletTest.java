package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetMenuPromptsServletTest extends ListenServletTest
{
    private GetMenuPromptsServlet servlet = new GetMenuPromptsServlet();

    @Test
    public void test_doGet_withNoCurrentUser_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withNonAdminCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }
}
