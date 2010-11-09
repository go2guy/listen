package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.attendant.Menu;

import java.io.IOException;

import javax.servlet.ServletException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

public class SaveAttendantMenuServletTest extends ListenServletTest
{
    private SaveAttendantMenuServlet servlet = new SaveAttendantMenuServlet();

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doPost_withNonAdminCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }

    @Test
    public void test_doPost_withValidNewMenuAndNotNamedTopMenu_savesMenu() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        String name = TestUtil.randomString();

        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("audioFile", TestUtil.randomString());

        JSONObject defaultAction = new JSONObject();
        defaultAction.put("action", "DialPressedNumber");
        json.put("defaultAction", defaultAction);

        JSONObject timeoutAction = new JSONObject();
        timeoutAction.put("action", "DialPressedNumber");
        json.put("timeoutAction", timeoutAction);

        JSONObject dialNumberAction = new JSONObject();
        dialNumberAction.put("action", "DialNumber");
        JSONObject args = new JSONObject();
        args.put("number", TestUtil.randomString());
        dialNumberAction.put("arguments", args);

        JSONArray actions = new JSONArray();
        actions.add(dialNumberAction);

        json.put("actions", actions);

        request.setParameter("menu", json.toJSONString());
        servlet.doPost(request, response);

        assertEquals(200, response.getStatus());

        boolean found = false;
        for(Menu menu : Menu.queryAll(session))
        {
            found |= menu.getName().equals(name);
        }
        assertTrue(found);
        
        // TODO apparently queryByName isn't coming back with the menu, not sure why
//        List<Menu> result = Menu.queryByName(session, name);
//        assertEquals(0, result.size());
    }
}
