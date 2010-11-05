package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.*;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Conference;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

public class GetConferenceListServletTest extends ListenServletTest
{
    private GetConferenceListServlet servlet = new GetConferenceListServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doGet_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Not logged in");
    }

    // TODO test with non-administrator subscriber
    
    @Test
    public void test_doGet_withAdminSubscriber_returnsJsonConferenceList() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Long count = Conference.count(session);
        servlet.doGet(request, response);

        assertOutputBufferContentTypeEquals("application/json");

        StringBuilder buffer = (StringBuilder)request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY);
        JSONObject output = (JSONObject)JSONValue.parse(buffer.toString());
        assertEquals(count, output.get("total"));
    }
}
