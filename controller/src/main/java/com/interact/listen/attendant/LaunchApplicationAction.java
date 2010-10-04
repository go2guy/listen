package com.interact.listen.attendant;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.Session;
import org.json.simple.JSONObject;

@Entity
public class LaunchApplicationAction extends Action
{
    @Column(name = "APPLICATION_NAME")
    private String applicationName;

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject args = new JSONObject();
        args.put("applicationName", applicationName);
        return createJsonObject("LaunchApplication", args);
    }
    
    @Override
    public String toIvrCommandJson(Session session)
    {
        JSONObject json = new JSONObject();
        json.put("action", "LAUNCH_APPLICATION");
        
        JSONObject args = new JSONObject();
        args.put("applicationName", applicationName);
        
        json.put("args", args);
        
        return json.toString();
    }
}
