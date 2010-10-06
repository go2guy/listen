package com.interact.listen.attendant;

import static org.junit.Assert.assertEquals;

import com.interact.listen.TestUtil;

import org.json.simple.JSONObject;
import org.junit.Test;

public class GoToMenuActionTest
{
    private GoToMenuAction action = new GoToMenuAction();

    @Test
    public void test_toJson_returnsJsonObject()
    {
        final Long id = TestUtil.randomNumeric(10);
        final String keyPressed = TestUtil.randomString();
        final Menu menu = new Menu();
        menu.setId(TestUtil.randomNumeric(10));

        action.setId(id);
        action.setKeyPressed(keyPressed);
        action.setGoToMenu(menu);

        JSONObject json = action.toJson();
        assertEquals(id, json.get("id"));
        assertEquals("GoToMenu", json.get("action"));
        assertEquals(keyPressed, json.get("keyPressed"));
        assertEquals(menu.getId(), ((JSONObject)json.get("arguments")).get("menuId"));
    }
}
