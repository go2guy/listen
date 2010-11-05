package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

    @Test
    public void test_doGet_retrievesJsonSubscriberList() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Long count = Subscriber.count(session);
        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());
        assertEquals(count, output.get("total"));
    }
}
