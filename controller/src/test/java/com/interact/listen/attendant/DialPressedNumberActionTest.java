package com.interact.listen.attendant;

import static org.junit.Assert.assertEquals;

import com.interact.listen.TestUtil;

import org.json.simple.JSONObject;
import org.junit.Test;

public class DialPressedNumberActionTest
{
    private DialPressedNumberAction action = new DialPressedNumberAction();
    
    @Test
    public void test_toJson_returnsJsonObject()
    {
        final Long id = TestUtil.randomNumeric(10);
        final String keyPressed = TestUtil.randomString();
        
        action.setId(id);
        action.setKeyPressed(keyPressed);
        
        JSONObject json = action.toJson();
        assertEquals(id, json.get("id"));
        assertEquals("DialPressedNumber", json.get("action"));
        assertEquals(keyPressed, json.get("keyPressed"));
    }
}
