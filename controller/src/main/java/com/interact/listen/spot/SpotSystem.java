package com.interact.listen.spot;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONValue;

/**
 * Represents a SPOT IVR system that this application can communicate with. Provides a defined set of operations that a
 * SPOT system allows via HTTP request.
 */
public class SpotSystem
{
    /** {@link Subscriber} performing the operations to this {@code SpotSystem} */
    private Subscriber performingSubscriber;

    private HttpClient httpClient = new HttpClientImpl();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
    private StatSender statSender = new InsaStatSender();

    public SpotSystem(Subscriber performingSubscriber)
    {
        this.performingSubscriber = performingSubscriber;
    }

    public void setHttpClient(HttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void setStatSender(StatSender statSender)
    {
        this.statSender = statSender;
    }

    /**
     * Deletes the provided file on the SPOT system.
     * 
     * @param filePath HTTP file URL to delete
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void deleteArtifact(String filePath) throws IOException, SpotCommunicationException
    {
        sendDeleteArtifactEvent("FILE", filePath);
    }

    /**
     * Deletes all artifacts on the SPOT system for the provided subscriber.
     * 
     * @param subscriber subscriber for which to delete artifacts
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void deleteAllSubscriberArtifacts(Subscriber subscriber) throws IOException, SpotCommunicationException
    {
        sendDeleteArtifactEvent("SUB", String.valueOf(subscriber.getId()));
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
        sendConferenceParticipantEvent("DROP", participant);
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
        sendConferenceParticipantEvent("MUTE", participant);
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
    public void outdial(String numbers, String adminSessionId, Long conferenceId, String requestingNumber, String interrupt)
        throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "AUTO_DIAL");
        importedValue.put("action", "DIAL");
        importedValue.put("sessionId", adminSessionId);
        importedValue.put("conferenceId", String.valueOf(conferenceId));
        importedValue.put("destination", numbers);
        importedValue.put("ani", requestingNumber);
        importedValue.put("interruptAdmin", interrupt);
        buildAndSendRequest(importedValue);
    }

    /**
     * Starts recording the provided {@code Conference}.
     * 
     * @param conference {@code Conference} to start recording
     * @param adminSessionId session id of the {@code Conference} administrator
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void startRecording(Conference conference, String adminSessionId) throws IOException,
        SpotCommunicationException
    {
        sendConferenceRecordingEvent("START", adminSessionId, conference);
    }

    /**
     * Stops recording the provided {@code Conference}.
     * 
     * @param conference {@code Conference} to stop recording
     * @param adminSessionId session id of the {@code Conference} administrator
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void stopRecording(Conference conference, String adminSessionId) throws IOException,
        SpotCommunicationException
    {
        sendConferenceRecordingEvent("STOP", adminSessionId, conference);
    }

    /**
     * Sends an event to the phone at the specified {@link AccessNumber} to turn the message light off.
     * 
     * @param accessNumber {@code AccessNumber} for which to turn message light off
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void turnMessageLightOff(AccessNumber accessNumber) throws IOException, SpotCommunicationException
    {
        sendMessageLightEvent("OFF", accessNumber);
    }

    /**
     * Sends an event to the phone at the specified {@link AccessNumber} to turn the message light on.
     * 
     * @param accessNumber {@code AccessNumber} for which to turn message light on
     * @throws IOException if an HTTP error occurs
     * @throws SpotCommunicationException if an error occurs communicating with the SPOT system
     */
    public void turnMessageLightOn(AccessNumber accessNumber) throws IOException, SpotCommunicationException
    {
        sendMessageLightEvent("ON", accessNumber);
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
        sendConferenceParticipantEvent("UNMUTE", participant);
    }

    private void sendConferenceParticipantEvent(String action, Participant participant) throws IOException,
        SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("action", action);
        importedValue.put("sessionId", participant.getSessionID());
        buildAndSendRequest(importedValue);
    }

    private void sendConferenceRecordingEvent(String action, String adminSessionId, Conference conference)
        throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "RECORD");
        importedValue.put("action", action);
        importedValue.put("sessionId", adminSessionId);
        importedValue.put("conferenceId", String.valueOf(conference.getId()));
        importedValue.put("startTime", sdf.format(conference.getStartTime()));
        importedValue.put("recordingSessionId", conference.getRecordingSessionId());
        importedValue.put("arcadeId", conference.getArcadeId());
        importedValue.put("description", conference.getDescription());
        buildAndSendRequest(importedValue);
    }

    private void sendDeleteArtifactEvent(String action, String filePath) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "DEL_ARTIFACT");
        importedValue.put("action", action);
        importedValue.put("artifact", filePath);
        buildAndSendRequest(importedValue);
    }

    private void sendMessageLightEvent(String action, AccessNumber accessNumber) throws IOException,
        SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "MSG_LIGHT"); // monosodium glutimate light, on!
        importedValue.put("action", action);
        importedValue.put("destination", accessNumber.getNumber());
        buildAndSendRequest(importedValue);
    }

    private void buildAndSendRequest(Map<String, Object> importedValue) throws IOException, SpotCommunicationException
    {
        if(performingSubscriber != null)
        {
            importedValue.put("initiatingSubscriber", Marshaller.buildHref(performingSubscriber));
        }
        importedValue.put("initiatingChannel", Channel.GUI.toString());

        Map<String, String> params = new TreeMap<String, String>();
        params.put("uri", "/interact/apps/iistart.ccxml");
        params.put("II_SB_importedValue", JSONValue.toJSONString(importedValue));
        sendRequest(params);
    }

    private void sendRequest(Map<String, String> params) throws IOException, SpotCommunicationException
    {
        // FIXME what happens when the first one succeeds and the second one fails? do we "rollback" the first one?
        // there's no way we can do it with 100% reliability (because the "rollback" might fail, too)
        // - in all likelihood there will only be one Spot subscriber, but we should accommodate many

        // TODO we should decouple the looping here, have some other class call this method with an argument of the
        // system to send the request to

        statSender.send(Stat.PUBLISHED_EVENT_TO_SPOT);

        Set<String> systems = Property.delimitedStringToSet(Configuration.get(Property.Key.SPOT_SYSTEMS), ",");
        for(String system : systems)
        {
            String uri = system + "/ccxml/createsession";
            httpClient.post(uri, params);

            int status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status))
            {
                throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System at [" + uri +
                                                     "]");
            }
        }
    }

    private boolean isSuccessStatus(int status)
    {
        return status >= 200 && status <= 299;
    }
}
