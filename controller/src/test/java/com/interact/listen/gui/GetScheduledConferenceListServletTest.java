package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ScheduledConference;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
        testForListenServletException(servlet, 400, "Id cannot be null");
    }

    @Test
    public void test_doGet_withBlankIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("GET");
        request.setParameter("id", "  ");
        testForListenServletException(servlet, 400, "Id must be a number");
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
    
    @Test
    public void test_doGet_withConferenceHavingOneScheduledConference_returnsJsonList() throws ServletException, IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, false, session);
        Conference conference = createConference(session, subscriber);
        
        ScheduledConference scheduled = createScheduledConference(session, conference, subscriber);
        
        request.setParameter("id", String.valueOf(conference.getId()));
        request.setParameter("historic", "true");
        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());
        assertEquals(1L, output.get("total"));

        JSONArray results = (JSONArray)output.get("results");
        JSONObject sc = (JSONObject)results.get(0);
        assertEquals(scheduled.getId(), sc.get("id"));
    }
}
