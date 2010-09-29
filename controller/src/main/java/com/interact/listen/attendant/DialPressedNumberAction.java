package com.interact.listen.attendant;

import javax.persistence.Entity;

import org.json.simple.JSONObject;

@Entity
public class DialPressedNumberAction extends Action
{
    @Override
    public JSONObject toJson()
    {
        return createJsonObject("DialPressedNumber", new JSONObject());
    }
}
