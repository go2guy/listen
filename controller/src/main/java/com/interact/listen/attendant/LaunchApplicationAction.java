package com.interact.listen.attendant;

import javax.persistence.Column;
import javax.persistence.Entity;

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
}
