package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetScheduledConferenceListServletTest extends ListenServletTest
{
    private GetScheduledConferenceListServlet servlet = new GetScheduledConferenceListServlet();

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
        request.setParameter("id", "  ");
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doGet_withNonexistentConferenceForId_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        Long id = TestUtil.randomNumeric(10);

        Conference conference = (Conference)session.get(Conference.class, id);
        assert conference == null;

        request.setMethod("GET");
        request.setParameter("id", String.valueOf(id));

        testForListenServletException(servlet, 400, "Conference not found");
    }
}
