package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

    @Test
    public void test_doGet_withSubsriberHavingOneVoicemail_returnsJsonVoicemailList() throws ServletException,
        IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, false, session);
        Voicemail voicemail = createVoicemail(session, subscriber);

        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());
        assertEquals(1L, output.get("total"));

        JSONArray results = (JSONArray)output.get("results");
        JSONObject vm = (JSONObject)results.get(0);

        assertEquals(voicemail.getId(), vm.get("id"));
    }
}
