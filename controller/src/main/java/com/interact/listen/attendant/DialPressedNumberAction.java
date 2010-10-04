package com.interact.listen.attendant;

import javax.persistence.Entity;

import org.hibernate.Session;
import org.json.simple.JSONObject;

@Entity
public class DialPressedNumberAction extends Action
{
    @Override
    public JSONObject toJson()
    {
        return createJsonObject("DialPressedNumber", new JSONObject());
    }
    
    @Override
    public String toIvrCommandJson(Session session)
    {
        JSONObject json = new JSONObject();
        json.put("action", "DIAL_PRESSED_NUMBER");
        
        return json.toString();
    }
}
