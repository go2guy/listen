package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetConferenceRecordingServletTest extends ListenServletTest
{
    private GetConferenceRecordingServlet servlet = new GetConferenceRecordingServlet();

    @Test
    public void test_doGet_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withNullIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("GET");
        request.setParameter("id", (String)null);
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doGet_withBlankIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("GET");
        request.setParameter("id", " ");
        testForListenServletException(servlet, 400, "Please provide an id");
    }
}
