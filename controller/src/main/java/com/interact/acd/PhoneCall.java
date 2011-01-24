package com.interact.acd;

import com.interact.acd.router.Destination;
import com.interact.acd.router.Parcel;
import com.interact.acd.router.RouteUnsuccessfulException;
import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.simple.JSONObject;

/**
 * Phone call that SPOT is holding onto. Presumably SPOT is waiting to send it to a particular phone.
 */
public class PhoneCall implements Parcel
{
    private final String spotSessionId;
    private final String spotSystemUrl; // e.g. http://example.com:80 - routeTo will append /spot/ccxml/createsession

    public PhoneCall(String spotSessionId, String spotSystemUrl)
    {
        this.spotSessionId = spotSessionId;
        this.spotSystemUrl = spotSystemUrl;
    }

    @Override
    public void routeTo(Destination destination) throws RouteUnsuccessfulException
    {
        // FIXME ultimately, this logic should be pulled out into a new SpotSystem().sendDialRequest() method, or
        // something
        JSONObject args = new JSONObject();
        args.put("action", "DIAL");
        args.put("sessionId", spotSessionId);
        args.put("destination", destination.getAddress());

        Map<String, String> requestParameters = new HashMap<String, String>();
        requestParameters.put("II_SB_importedValue", args.toJSONString());
        requestParameters.put("uri", "/interact/apps/iistart.ccxml");

        HttpClient client = new HttpClientImpl(); // FIXME remove dependency on listen library
        try
        {
            client.post(spotSystemUrl + "/spot/ccxml/createsession", requestParameters);
        }
        catch(IOException e)
        {
            throw new RouteUnsuccessfulException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        if(obj == this)
        {
            return true;
        }

        if(obj.getClass() != this.getClass())
        {
            return false;
        }

        PhoneCall that = (PhoneCall)obj;

        EqualsBuilder eqb = new EqualsBuilder();
        eqb.append(spotSessionId, that.spotSessionId);
        return eqb.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder hcb = new HashCodeBuilder(443, 1039); // randomly-chosen primes
        hcb.append(spotSessionId);
        return hcb.toHashCode();
    }
}
