package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class ScheduleConferenceServletTest extends ListenServletTest
{
    private ScheduleConferenceServlet servlet = new ScheduleConferenceServlet();

    private static final String[] ALL_REQUEST_PARAMETERS =
                                                           new String[] { "date", "hour", "minute", "amPm", "endHour",
                                                                         "endMinute", "endAmPm", "subject",
                                                                         "description", "activeParticipants",
                                                                         "passiveParticipants" };

    @Before
    public void setUp()
    {
        setAllRequestParametersToRandomStuff(request);
    }

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized");
    }

    @Test
    public void test_doPost_nullOrEmptyDate_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "date", "Please provide a date");
    }

    @Test
    public void test_doPost_nullOrEmptyHour_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "hour", "Please provide an hour for the conference start time");
    }

    @Test
    public void test_doPost_nullOrEmptyMinute_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "minute", "Please provide a minute for the conference start time");
    }

    @Test
    public void test_doPost_nullOrEmptyAmPm_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "amPm", "Please provide an am/pm for the conference start time");
    }

    @Test
    public void test_doPost_nullOrEmptyEndHour_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "endHour", "Please provide an hour for the conference end time");
    }

    @Test
    public void test_doPost_nullOrEmptyEndMinute_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "endMinute",
                                             "Please provide a minute for the conference end time");
    }

    @Test
    public void test_doPost_nullOrEmptyEndAmPm_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNullOrEmpty(request, "endAmPm", "Please provide an am/pm for the conference end time");
    }

    @Test
    public void test_doPost_nullDescription_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNull(request, "description", "Please provide a description");
    }

    @Test
    public void test_doPost_nullActiveParticipants_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNull(request, "activeParticipants",
                                      "Please provide a comma-separated list of active participants");
    }

    @Test
    public void test_doPost_nullPassiveParticipants_throwsBadRequest() throws ServletException, IOException
    {
        testRequestParameterIsNotNull(request, "passiveParticipants",
                                      "Please provide a comma-separated list of passive participants");
    }

    @Test
    public void test_doPost_bothActiveAndPassiveParticipantsEmpty_throwsBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("activeParticipants", "");
        request.setParameter("passiveParticipants", "");

        testForListenServletException(servlet, 400,
                                      "Please provide at least one active or passive participant email address");
    }

    private void testRequestParameterIsNotNullOrEmpty(MockHttpServletRequest request, String parameterName,
                                                      String expectedMessage) throws ServletException, IOException
    {
        testRequestParameterIsNotNull(request, parameterName, expectedMessage);
        testRequestParameterIsNotEmpty(request, parameterName, expectedMessage);
    }

    private void testRequestParameterIsNotNull(MockHttpServletRequest request, String parameterName,
                                               String expectedMessage) throws ServletException, IOException
    {
        if(ServletUtil.currentSubscriber(request) == null)
        {
            TestUtil.setSessionSubscriber(request, false, session);
        }
        request.setMethod("POST");
        request.setParameter(parameterName, (String)null);
        testForListenServletException(servlet, 400, expectedMessage);
    }

    private void testRequestParameterIsNotEmpty(MockHttpServletRequest request, String parameterName,
                                                String expectedMessage) throws ServletException, IOException
    {
        if(ServletUtil.currentSubscriber(request) == null)
        {
            TestUtil.setSessionSubscriber(request, false, session);
        }
        request.setMethod("POST");
        request.setParameter(parameterName, " ");
        testForListenServletException(servlet, 400, expectedMessage);
    }

    private void setAllRequestParametersToRandomStuff(MockHttpServletRequest request)
    {
        for(String property : ALL_REQUEST_PARAMETERS)
        {
            request.setParameter(property, TestUtil.randomString());
        }
    }
}
