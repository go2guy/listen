package com.interact.listen.android.voicemail.client;

import org.json.JSONException;
import org.json.JSONObject;

public final class ServerRegistrationInfo
{
    private final boolean enabled;
    private final String senderId;
    private final String registrationId;

    ServerRegistrationInfo(JSONObject json) throws JSONException
    {
        if(json == null)
        {
            throw new JSONException("JSON registration information not set");
        }

        enabled  = json.has("enabled") ? json.getBoolean("enabled") : false;
        senderId = json.has("account") ? json.getString("account")  : "";
        registrationId = json.has("registrationToken") ? json.getString("registrationToken")  : "";
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    public String getSenderId()
    {
        return senderId;
    }
    
    public String getRegistrationId()
    {
        return registrationId;
    }
    
    @Override
    public String toString()
    {
        return "[ServerRegistrationInfo enabled=" + enabled + " sender='" + senderId + "' reg='" + registrationId + "']";
    }
}
