package com.interact.listen.android.voicemail.client;

import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.sync.Authority;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ServerRegistrationInfo
{
    private final boolean enabled;
    private final String senderId;
    private final String registrationId;
    private final Authority[] authorities;
    
    ServerRegistrationInfo(JSONObject json) throws JSONException
    {
        if(json == null)
        {
            throw new JSONException("JSON registration information not set");
        }

        enabled  = json.has("enabled") ? json.getBoolean("enabled") : false;
        senderId = json.has("account") ? json.getString("account")  : "";
        registrationId = json.has("registrationToken") ? json.getString("registrationToken")  : "";
        
        JSONArray jArray = (JSONArray)json.opt("registeredTypes");
        if(jArray == null)
        {
            // legacy
            authorities = new Authority[] {Authority.VOICEMAIL};
        }
        else
        {
            ArrayList<Authority> auths = new ArrayList<Authority>(jArray.length());
            for(int i = 0; i < jArray.length(); ++i)
            {
                try
                {
                    Authority auth = Authority.valueOf(jArray.getString(i));
                    auths.add(auth);
                }
                catch(Exception e)
                {
                    Log.w(Constants.TAG, "unknown authority", e);
                }
            }
            authorities = auths.toArray(new Authority[auths.size()]);
        }
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
    
    public Authority[] getRegisteredAuthorities()
    {
        return authorities.clone();
    }
    
    public boolean isRegistered(Authority auth)
    {
        for(Authority a : authorities)
        {
            if(a == auth)
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        return "[ServerRegistrationInfo enabled=" + enabled + " sender='" + senderId +
               "' reg='" + registrationId + "' auths=" + authorities + "]";
    }
}
