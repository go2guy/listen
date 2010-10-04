package com.interact.listen.attendant;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.Session;
import org.json.simple.JSONObject;

@Entity
public class DialNumberAction extends Action
{
    @Column(name = "NUMBER")
    private String number;

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject args = new JSONObject();
        args.put("number", number);
        return createJsonObject("DialNumber", args);
    }
    
    @Override
    public String toIvrCommandJson(Session session)
    {
        JSONObject json = new JSONObject();
        json.put("action", "DIAL_NUMBER");
        
        JSONObject args = new JSONObject();
        args.put("number", number);
        
        json.put("args", args);
        
        return json.toString();
    }
}
