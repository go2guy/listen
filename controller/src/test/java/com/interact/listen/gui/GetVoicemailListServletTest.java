package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetVoicemailListServletTest extends ListenServletTest
{
    private GetVoicemailListServlet servlet = new GetVoicemailListServlet();
    
    @Test
    public void test_doGet_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }
}
