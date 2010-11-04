package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.attendant.Action;
import com.interact.listen.attendant.Menu;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

public class GetAttendantMenuListServletTest extends ListenServletTest
{
    GetAttendantMenuListServlet servlet = new GetAttendantMenuListServlet();

    @Test
    public void test_doGet_withoutCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withNonAdministratorCurrentSubscriber_throwsUnauthorized() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }

    @Test
    public void test_doGet_withValidUserAndParameters_appendsMenuJsonToOutputBuffer() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Menu menu = createMenu(session);
        Action action = createAction(session, "DialPressedNumberAction", menu, "123", null);
        menu.setDefaultAction(action);
        menu.setTimeoutAction(action);
        session.save(menu);

        request.setMethod("GET");
        servlet.doGet(request, response);

        JSONArray json = (JSONArray)JSONValue.parse(request.getOutputBufferString());
        boolean found = false;
        for(Object obj : json)
        {
            JSONObject o = (JSONObject)obj;
            if(o.get("id").equals(menu.getId()))
            {
                found = true;
                assertEquals(menu.getAudioFile(), o.get("audioFile"));
                assertEquals(menu.getName(), o.get("name"));
            }
        }
        assertOutputBufferContentTypeEquals("application/json");
        assertTrue(found);
    }
}
