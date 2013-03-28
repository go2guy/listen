package com.interact.listen.spot

import com.interact.listen.User
import com.interact.listen.history.Channel
import com.interact.listen.httpclient.HttpClientImpl
import com.interact.listen.stats.Stat
import org.joda.time.format.DateTimeFormat

class SpotCommunicationService {
    static transactional = false

    def springSecurityService
    def statWriterService

    def dropParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("DROP", participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_DROP)
    }

    def muteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("MUTE", participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_MUTE)
    }

    def outdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "AUTO_DIAL");
        importedValue.put("action", "DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("conferenceId", conference.id);
        importedValue.put("destination", numbers);
        importedValue.put("organization", "/organizations/${conference.owner.organization.id}")
        importedValue.put("ani", requestingNumber);
        buildAndSendRequest(importedValue);
        statWriterService.send(Stat.SPOT_AUTO_DIAL_DIAL)
    }

    def interactiveOutdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("action", "INTERACTIVE_DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("destination", numbers);
        importedValue.put("ani", requestingNumber);
        importedValue.put("organization", "/organizations/${conference.owner.organization.id}")
        buildAndSendRequest(importedValue);
        statWriterService.send(Stat.SPOT_CONF_EVENT_BRIDGE_DIAL)
    }

    def startRecording(def conference) throws IOException, SpotCommunicationException {
        sendConferenceRecordingEvent("START", conference);
        statWriterService.send(Stat.SPOT_RECORD_START)
    }

    def stopRecording(def conference) throws IOException, SpotCommunicationException {
        sendConferenceRecordingEvent("STOP", conference);
        statWriterService.send(Stat.SPOT_RECORD_STOP)
    }

    def toggleMessageLight(def number, def ip, boolean on) throws IOException, SpotCommunicationException {
        sendMessageLightEvent(on ? 'ON' : 'OFF', number, ip)
        statWriterService.send(on ? Stat.SPOT_MSG_LIGHT_ON : Stat.SPOT_MSG_LIGHT_OFF)
    }

    def unmuteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("UNMUTE", participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_UNMUTE)
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

    def sendFax(def fax) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "Direct Message");
        importedValue.put("action", "PDF_TO_TIFF");
        importedValue.put("artifact", fax.sourceFiles.collect{ it.file.absolutePath });
        importedValue.put("destination", fax.dnis);
        importedValue.put("ani", ""); //Will be implemented later, probably once sold and we know what to do
        importedValue.put("organization", "/organizations/${fax.sender.organization.id}");
        importedValue.put("id", fax.id)
        buildAndSendRequest(importedValue);
    }

    private void sendMessageLightEvent(def action, def number, def ip) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "MSG_LIGHT"); // monosodium glutimate light, on!
        importedValue.put("action", action);
        importedValue.put("destination", number);
        importedValue.put('ip', ip)
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
        int status = -1
        SpotSystem.findAll().each {
            def httpClient = new HttpClientImpl()

            String uri = it.name + "/ccxml/createsession";
            httpClient.post(uri, params);

            status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status)) {
                failed << it
            }
        }
        if(failed.size() > 0) {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System(s) at [" + (failed.collect { it.uri }.join(',')) + "]");
        }
        
        log.debug "Completed sendRequest [${failed.size()}]"
    }

    private boolean isSuccessStatus(int status) {
        return status >= 200 && status <= 299;
    }
} 
