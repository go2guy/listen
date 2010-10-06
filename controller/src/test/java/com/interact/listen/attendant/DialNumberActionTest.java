package com.interact.listen.attendant;

import static org.junit.Assert.assertEquals;

import com.interact.listen.TestUtil;

import org.json.simple.JSONObject;
import org.junit.Test;

public class DialNumberActionTest
{
    private DialNumberAction action = new DialNumberAction();

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = TestUtil.randomString();
        action.setNumber(number);
        assertEquals(number, action.getNumber());
    }

    @Test
    public void test_toJson_returnsJsonObject()
    {
        final Long id = TestUtil.randomNumeric(10);
        final String keyPressed = TestUtil.randomString();
        final String number = TestUtil.randomString();

        action.setId(id);
        action.setKeyPressed(keyPressed);
        action.setNumber(number);

        JSONObject json = action.toJson();
        assertEquals(id, json.get("id"));
        assertEquals("DialNumber", json.get("action"));
        assertEquals(keyPressed, json.get("keyPressed"));
        assertEquals(number, ((JSONObject)json.get("arguments")).get("number"));
    }
}
