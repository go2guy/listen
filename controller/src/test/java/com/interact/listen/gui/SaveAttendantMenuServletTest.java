package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class SaveAttendantMenuServletTest extends ListenServletTest
{
    private SaveAttendantMenuServlet servlet = new SaveAttendantMenuServlet();

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doPost_withNonAdminCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }
}
