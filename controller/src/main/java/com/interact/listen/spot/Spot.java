package com.interact.listen.spot;

import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.resource.Participant;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Spot
{
    // e.g. "http://apps2/spot/ccxml/basichttp"
    private String httpInterfaceUri;

    public Spot(String httpInterfaceUri)
    {
        this.httpInterfaceUri = httpInterfaceUri;
    }

    private enum SpotRequestEvent
    {
        DROP_PARTICIPANT("Drop"), MUTE_PARTICIPANT("Mute"), UNMUTE_PARTICIPANT("Unmute");

        private String eventName;

        private SpotRequestEvent(String eventName)
        {
            this.eventName = eventName;
        }
    }

    public void dropParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.DROP_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", ""); // FIXME put a value here
        sendSpotRequest(params);
    }

    public void muteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.MUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", ""); // FIXME put a value here
        sendSpotRequest(params);
    }

    public void unmuteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.UNMUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", ""); // FIXME put a value here
        sendSpotRequest(params);
    }

    private void sendSpotRequest(Map<String, String> params) throws IOException, SpotCommunicationException
    {
        HttpClientImpl client = new HttpClientImpl();
        client.post(httpInterfaceUri, params);

        Integer status = client.getResponseStatus();
        if(!isSuccessStatus(status))
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT");
        }
    }

    private boolean isSuccessStatus(Integer status)
    {
        if(status == null)
        {
            return false;
        }
        return status >= 200 && status <= 299;
    }
}
