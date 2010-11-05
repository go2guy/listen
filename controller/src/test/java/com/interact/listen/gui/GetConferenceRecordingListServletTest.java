package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ConferenceRecording;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

public class GetConferenceRecordingListServletTest extends ListenServletTest
{
    private GetConferenceRecordingListServlet servlet = new GetConferenceRecordingListServlet();

    @Test
    public void test_doGet_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withConferenceNotFoundAndCurrentSubscriberHasNoConferences_throwsInternalServerError()
        throws ServletException, IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, false, session);
        assert subscriber.getConferences().size() == 0;

        Long id = System.currentTimeMillis();

        Conference conference = (Conference)session.get(Conference.class, id);
        assert conference == null;

        request.setMethod("GET");
        request.setParameter("id", String.valueOf(id));
        testForListenServletException(servlet, 500, "Conference not found");
    }

    @Test
    public void test_doGet_withAdminCurrentUser_returnsJsonListOfConferenceRecordings() throws ServletException,
        IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, true, session);
        Conference conference = createConference(session, subscriber);

        ConferenceRecording recording = new ConferenceRecording();
        recording.setConference(conference);
        recording.setDateCreated(new Date());
        recording.setDescription(TestUtil.randomString());
        recording.setDuration(String.valueOf(TestUtil.randomNumeric(10)));
        recording.setFileSize(String.valueOf(TestUtil.randomNumeric(10)));
        recording.setTranscription(TestUtil.randomString());
        recording.setUri(TestUtil.randomString());
        session.save(recording);

        request.setParameter("id", String.valueOf(conference.getId()));
        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        System.out.println("BUFFER: " + buffer);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());

        assertEquals(1L, output.get("total"));

        JSONArray results = (JSONArray)output.get("results");
        JSONObject r = (JSONObject)results.get(0);

        assertEquals(recording.getId(), r.get("id"));
    }
}
