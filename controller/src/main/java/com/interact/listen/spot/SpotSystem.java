package com.interact.listen.spot;

import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.resource.Participant;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpotSystem
{
    // e.g. "http://apps2/spot/ccxml/basichttp"
    private String httpInterfaceUri;

    private HttpClient httpClient = new HttpClientImpl();
    private StatSender statSender = new InsaStatSender();

    public SpotSystem(String httpInterfaceUri)
    {
        this.httpInterfaceUri = httpInterfaceUri;
    }

    public void setHttpClient(HttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void setStatSender(StatSender statSender)
    {
        this.statSender = statSender;
    }

    private enum SpotRequestEvent
    {
        DROP_PARTICIPANT("DROP"), MUTE_PARTICIPANT("MUTE"), UNMUTE_PARTICIPANT("UNMUTE");

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
        params.put("II_SB_valueToPass", "");
        sendSpotRequest(params);
    }

    public void muteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.MUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", "");
        sendSpotRequest(params);
    }

    public void unmuteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.UNMUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", "");
        sendSpotRequest(params);
    }

    private void sendSpotRequest(Map<String, String> params) throws IOException, SpotCommunicationException
    {
        statSender.send(Stat.PUBLISHED_EVENT_TO_SPOT);
        httpClient.post(httpInterfaceUri, params);

        int status = httpClient.getResponseStatus();
        if(!isSuccessStatus(status))
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System at [" +
                                                 httpInterfaceUri + "]");
        }
    }

    private boolean isSuccessStatus(int status)
    {
        return status >= 200 && status <= 299;
    }
}
