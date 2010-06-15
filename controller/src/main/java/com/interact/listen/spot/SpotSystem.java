package com.interact.listen.spot;

import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Participant;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a SPOT IVR system that this application can communicate with. Provides a defined set of operations that a
 * SPOT system allows via HTTP request.
 */
public class SpotSystem
{
    // e.g. "http://apps2/spot"
    private String httpInterfaceUri;

    private HttpClient httpClient = new HttpClientImpl();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
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
        DROP_PARTICIPANT("DROP"), MUTE_PARTICIPANT("MUTE"), OUTDIAL("AUTO_DIAL"), START_RECORDING("START_REC"), STOP_RECORDING("STOP_REC"),
        UNMUTE_PARTICIPANT("UNMUTE");

        private String eventName;

        private SpotRequestEvent(String eventName)
        {
            this.eventName = eventName;
        }
    }

    /**
     * Drops the provided {@link Participant} from the {@link Conference} they are in.
     * 
     * @param participant {@code Participant} to drop
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void dropParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.DROP_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", "");
        sendBasicHttpRequest(params, "basichttp");
    }

    /**
     * Mutes the provided {@link Participant}.
     * 
     * @param participant {@code Participant} to mute
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void muteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.MUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", "");
        sendBasicHttpRequest(params, "basichttp");
    }

    /**
     * "Outdials" a caller into the {@code Conference} containing the provided administrator session id. This causes the
     * SPOT system to make a phone call to the provided number. The called party receives a recorded message asking them
     * to join the conference.
     * 
     * @param numbers phone number to call
     * @param adminSessionId session id of the {@code Conference} administrator
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void outdial(String numbers, String adminSessionId, Long conferenceId, String requestingNumber) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uri", "/interact/apps/iistart.ccxml");
        params.put("II_SB_importedValue", "{\"application\":\"" + SpotRequestEvent.OUTDIAL.eventName +
                                          "\",\"sessionid\":\"" + adminSessionId + "\",\"destination\":\"" + numbers +
                                          "\",\"conferenceId\":\"" + String.valueOf(conferenceId) + "\",\"ani\":\"" +
                                          requestingNumber + "\"}");
        
        sendBasicHttpRequest(params, "createsession");
    }
    
    /**
     * Starts recording the provided {@code Conference}.
     * 
     * @param conference {@code Conference} to start recording
     * @param adminSessionId session id of the {@code Conference} administrator
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void startRecording(Conference conference, String adminSessionId) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", adminSessionId);
        params.put("name", "dialog.user.basichttp");
        params.put("II_SB_basichttpEvent", "CREATESESSION");
        params.put("II_SB_argument", "{\"application\":\"RECORD\",\"action\":\"" +
                                     SpotRequestEvent.START_RECORDING.eventName + "\",\"conferenceId\":\"" +
                                     String.valueOf(conference.getId()) + "\",\"startTime\":\"" +
                                     sdf.format(conference.getStartTime()) +
                                     "\",\"interface\":\"GUI\",\"recordingSessionId\":\"" +
                                     conference.getRecordingSessionId() + "\",\"arcadeId\":\"" +
                                     conference.getArcadeId() + "\",\"adminSID\":\"" + adminSessionId + "\"}");
        params.put("II_SB_URI", "listen_main/listen_main.ccxml");
        
        sendBasicHttpRequest(params, "basichttp");
    }
    
    /**
     * Stops recording the provided {@code Conference}.
     * 
     * @param conference {@code Conference} to stop recording
     * @param adminSessionId session id of the {@code Conference} administrator
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void stopRecording(Conference conference, String adminSessionId) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", adminSessionId);
        params.put("name", "dialog.user.basichttp");
        params.put("II_SB_basichttpEvent", "CREATESESSION");
        params.put("II_SB_argument", "{\"application\":\"RECORD\",\"action\":\"" +
                   SpotRequestEvent.STOP_RECORDING.eventName + "\",\"conferenceId\":\"" +
                   String.valueOf(conference.getId()) + "\",\"startTime\":\"" +
                   sdf.format(conference.getStartTime()) +
                   "\",\"interface\":\"GUI\",\"recordingSessionId\":\"" +
                   conference.getRecordingSessionId() + "\",\"arcadeId\":\"" +
                   conference.getArcadeId() + "\",\"adminSID\":\"" + adminSessionId + "\"}");
        params.put("II_SB_URI", "listen_main/listen_main.ccxml");
        
        sendBasicHttpRequest(params, "basichttp");
    }

    /**
     * Unmutes the provided {@link Participant}.
     * 
     * @param participant {@code Participant} to unmute
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void unmuteParticipant(Participant participant) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sessionid", participant.getSessionID());
        params.put("name", "dialog.user.customEvent");
        params.put("II_SB_eventToPass", SpotRequestEvent.UNMUTE_PARTICIPANT.eventName);
        params.put("II_SB_valueToPass", "");
        sendBasicHttpRequest(params, "basichttp");
    }

    private void sendBasicHttpRequest(Map<String, String> params, String target) throws IOException, SpotCommunicationException
    {
        statSender.send(Stat.PUBLISHED_EVENT_TO_SPOT);
        
        String uri = httpInterfaceUri + "/ccxml/" + target;
        httpClient.post(uri, params);

        int status = httpClient.getResponseStatus();
        if(!isSuccessStatus(status))
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System at [" +
                                                 uri + "]");
        }
    }
    
    private boolean isSuccessStatus(int status)
    {
        return status >= 200 && status <= 299;
    }
}
