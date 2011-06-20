package com.interact.listen.android;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

class C2DMessage
{
    private static final String PARAM_REGISTRATION_ID = "registration_id";
    private static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";
    private static final String PARAM_COLLAPSE_KEY = "collapse_key";

    private static final String UTF8 = "UTF-8";

    private final String registrationId;
    private final String collapseKey;
    private final Map<String, String[]> params;
    private final boolean delayWhileIdle;
    
    private byte[] postData;

    public C2DMessage(String registrationId, String collapseKey, Map<String, String[]> params, boolean delayWhileIdle)
    {
        this.registrationId = registrationId;
        this.collapseKey = collapseKey;
        this.params = params == null ? new HashMap<String, String[]>() : params;
        this.delayWhileIdle = delayWhileIdle;
        
        this.postData = null;
    }

    public String getRegistrationId()
    {
        return registrationId;
    }

    public String getCollapseKey()
    {
        return collapseKey;
    }

    public Map<String, String[]> getParams()
    {
        return params;
    }

    public boolean isDelayWhileIdle()
    {
        return delayWhileIdle;
    }

    public byte[] createPostData() throws UnsupportedEncodingException
    {
        if(postData == null)
        {
            StringBuilder builder = new StringBuilder();
            builder.append(PARAM_REGISTRATION_ID).append("=").append(registrationId);
    
            if(delayWhileIdle)
            {
                builder.append("&").append(PARAM_DELAY_WHILE_IDLE).append("=1");
            }
            builder.append("&").append(PARAM_COLLAPSE_KEY).append("=").append(collapseKey);
    
            if(params != null)
            {
                for(Object keyObj : params.keySet())
                {
                    String key = (String)keyObj;
                    if(key.startsWith("data."))
                    {
                        String[] values = (String[])params.get(key);
                        builder.append("&").append(key).append("=").append(URLEncoder.encode(values[0], UTF8));
                    }
                }
            }
            postData = builder.toString().getBytes(UTF8);
        }
        return postData;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(!(obj instanceof C2DMessage))
        {
            return false;
        }
        
        C2DMessage m = (C2DMessage)obj;

        if(!isEqual(this.registrationId, m.registrationId))
        {
            return false;
        }
        if(!isEqual(this.collapseKey, m.collapseKey))
        {
            return false;
        }
        
        if(this.params.size() != m.params.size())
        {
            return false;
        }
        
        Set<String> mKeys = this.params.keySet();
        for(String key : mKeys)
        {
            if(!m.params.containsKey(key) || !Arrays.deepEquals(this.params.get(key), m.params.get(key)))
            {
                return false;
            }
        }
        
        return this.delayWhileIdle == m.delayWhileIdle;
    }
    
    @Override
    public int hashCode()
    {
        return registrationId == null ? 0 : registrationId.hashCode();
    }

    // TODO copied from the old listen; should be replaced with equals() using
    // an EqualsBuilder and HashCodeBuilder (from apache.commons.lang)
    public static boolean isEqual(Object object1, Object object2)
    {
        if(object1 == null && object2 == null)
        {
            return true;
        }

        if(object1 != null && object1.equals(object2))
        {
            return true;
        }

        return false;
    }
}
