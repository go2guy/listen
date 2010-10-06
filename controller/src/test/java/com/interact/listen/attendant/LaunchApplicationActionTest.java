package com.interact.listen.attendant;

import static org.junit.Assert.assertEquals;

import com.interact.listen.TestUtil;

import org.json.simple.JSONObject;
import org.junit.Test;

public class LaunchApplicationActionTest
{
    private LaunchApplicationAction action = new LaunchApplicationAction();

    @Test
    public void test_toJson_returnsJsonObject()
    {
        final Long id = TestUtil.randomNumeric(10);
        final String keyPressed = TestUtil.randomString();
        final String applicationName = TestUtil.randomString();

        action.setId(id);
        action.setKeyPressed(keyPressed);
        action.setApplicationName(applicationName);

        JSONObject json = action.toJson();
        assertEquals(id, json.get("id"));
        assertEquals("LaunchApplication", json.get("action"));
        assertEquals(keyPressed, json.get("keyPressed"));
        assertEquals(applicationName, ((JSONObject)json.get("arguments")).get("applicationName"));
    }
}
