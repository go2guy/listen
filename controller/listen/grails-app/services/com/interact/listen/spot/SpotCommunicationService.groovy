package com.interact.listen.spot

import com.interact.listen.User
import com.interact.listen.history.Channel
import com.interact.listen.httpclient.HttpClientImpl
import org.joda.time.format.DateTimeFormat

class SpotCommunicationService {
    static scope = 'singleton'
    static transactional = false

    def springSecurityService

    def deleteArtifact(def filePath) throws IOException, SpotCommunicationException {
        sendDeleteArtifactEvent("FILE", filePath)
    }

    def deleteAllSubscriberArtifacts(def user) throws IOException, SpotCommunicationException {
        sendDeleteArtifactEvent("SUB", String.valueOf(user.id));
    }

    def dropParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("DROP", participant);
    }

    def muteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("MUTE", participant);
    }

    def outdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "AUTO_DIAL");
        importedValue.put("action", "DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("conferenceId", conference.id);
        importedValue.put("destination", numbers);
        importedValue.put("ani", requestingNumber);
        buildAndSendRequest(importedValue);
    }

    def interactiveOutdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("action", "INTERACTIVE_DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("destination", numbers);
        importedValue.put("ani", requestingNumber);
        buildAndSendRequest(importedValue);
    }

    def startRecording(def conference) throws IOException, SpotCommunicationException {
        sendConferenceRecordingEvent("START", conference);
    }

    def stopRecording(def conference) throws IOException, SpotCommunicationException {
        sendConferenceRecordingEvent("STOP", conference);
    }

    def toggleMessageLight(def number, boolean on) throws IOException, SpotCommunicationException {
        sendMessageLightEvent(on ? 'ON' : 'OFF', number)
    }

    def unmuteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("UNMUTE", participant);
    }

    def sendConferenceParticipantEvent(def action, def participant) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("action", action);
        importedValue.put("sessionId", participant.sessionId);
        buildAndSendRequest(importedValue);
    }

    def sendConferenceRecordingEvent(def action, def conference) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "RECORD");
        importedValue.put("action", action);
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("conferenceId", conference.id);
        importedValue.put("startTime", DateTimeFormat.forPattern('yyyyMMddhhmmssSSS').print(conference.startTime));
        importedValue.put("recordingSessionId", conference.recordingSessionId);
        importedValue.put("arcadeId", conference.arcadeId);
        importedValue.put("description", conference.description);
        buildAndSendRequest(importedValue);
    }

    private void sendDeleteArtifactEvent(def action, def filePath) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "DEL_ARTIFACT");
        importedValue.put("action", action);
        importedValue.put("artifact", filePath);
        buildAndSendRequest(importedValue);
    }

    private void sendMessageLightEvent(def action, def number) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "MSG_LIGHT"); // monosodium glutimate light, on!
        importedValue.put("action", action);
        importedValue.put("destination", number);
        buildAndSendRequest(importedValue);
    }

    private void buildAndSendRequest(Map<String, Object> importedValue) throws IOException, SpotCommunicationException
    {
        try {
            def user = springSecurityService.getCurrentUser()
            if(user)
            {
                importedValue.put("initiatingSubscriber", "${user.id}");
            }
        } catch(MissingPropertyException e) {
            // handles a non-User principal
            log.debug 'MissingPropertyException while building SPOT request, probably an API user'
        }
        importedValue.put("initiatingChannel", Channel.GUI.toString());

        Map<String, String> params = new TreeMap<String, String>();
        params.put("uri", "/interact/apps/iistart.ccxml");
        def json = importedValue.encodeAsJSON()
        params.put("II_SB_importedValue", json);
        sendRequest(params);
    }

    private void sendRequest(Map<String, String> params) throws IOException, SpotCommunicationException {
        log.debug "Sending SPOT HTTP request with params ${params} to ${SpotSystem.count()} SPOT systems"
        def failed = []
        SpotSystem.findAll().each {
            def httpClient = new HttpClientImpl()

            String uri = it.name + "/ccxml/createsession";
            httpClient.post(uri, params);

            int status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status)) {
                failed << it
            }
        }
        if(failed.size() > 0) {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System(s) at [" + (failed.collect { it.uri }.join(',')) + "]");
        }
    }

    private boolean isSuccessStatus(int status) {
        return status >= 200 && status <= 299;
    }
} 
