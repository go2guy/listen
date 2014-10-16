package com.interact.listen.spot

import com.interact.listen.User
import com.interact.listen.history.Channel
import com.interact.listen.httpclient.HttpClientImpl
import com.interact.listen.stats.Stat
import grails.converters.JSON
import org.joda.time.format.DateTimeFormat

class SpotCommunicationService {
    static transactional = false

    def grailsApplication
    def springSecurityService
    def statWriterService

    def dropParticipant(def participant) throws IOException, SpotCommunicationException {
        def success = false
        try {
            log.error("Attempting to drop participant [${participant.id}]")
            sendConferenceParticipantEvent("DROP", participant);
            statWriterService.send(Stat.SPOT_CONF_EVENT_DROP)
            success = true
        } catch (Exception e) {
            log.error("We've encounted an error attempting to drop participant [${participant.id}][${e}]")
        }
        return success
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

    /**
     * Send request to SPOT to connect a queued caller to a number.
     *
     * @param sessionId The sessionId of the call.
     * @param number The number to connect to.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdConnectEvent(def sessionId, def number) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending AcdConnectEvent, sessionId[" + sessionId + "], number[" + number + "]")
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "CONNECT");
        importedValue.put("sessionId", sessionId);
        importedValue.put("number", number);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT to disconnect a caller.
     *
     * @param sessionId The sessionId of the call.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdDisconnectEvent(def sessionId) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending AcdDisconnectEvent, sessionId[" + sessionId + "]");
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "DISCONNECT");
        importedValue.put("sessionId", sessionId);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT with a generic event
     *
     * @param sessionId The sessionId of the call.
     * @param event The action to use
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdGenericEvent(def sessionId, def event) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending AcdGenericEvent, sessionId[" + sessionId + "], event[" + event + "]");
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", event);
        importedValue.put("sessionId", sessionId);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT to connect a queued caller to voicemail.
     *
     * @param sessionId The sessionId of the call.
     * @param number The number to connect to.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdVoicemailEvent(def sessionId, def number) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending Acd Voicemail Event, sessionId[" + sessionId + "], number[" + number + "]")
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "VOICEMAIL");
        importedValue.put("sessionId", sessionId);
        importedValue.put("number", number);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT to put a caller on hold.
     *
     * @param sessionId The sessionId of the call.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdOnHoldEvent(def sessionId) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending Acd OnHold Event, sessionId[" + sessionId + "]");
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "ON_HOLD");
        importedValue.put("sessionId", sessionId);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT to take a caller off hold.
     *
     * @param sessionId The sessionId of the call.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdOffHoldEvent(def sessionId) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending Acd OffHold Event, sessionId[" + sessionId + "]");
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "OFF_HOLD");
        importedValue.put("sessionId", sessionId);
        buildAndSendRequest(importedValue);
    }

    /**
     * Send request to SPOT to switch a callers ACD Queue.
     *
     * @param sessionId The sessionId of the call.
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdSwitchQueueEvent(def sessionId, def onHoldMsg, def onHoldMusic, def connectMsg, def onHoldMsgExtended)
        throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending Acd Switch Queue Event, sessionId[" + sessionId + "]");
        }

        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "ACD");
        importedValue.put("action", "SWITCH_QUEUE");
        importedValue.put("sessionId", sessionId);
        importedValue.put("onHoldMsg", onHoldMsg);
        importedValue.put("onHoldMusic", onHoldMusic);
        importedValue.put("connectMsg", connectMsg);
        importedValue.put("onHoldMsgExtended", onHoldMsgExtended);
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
        try
        {
            def user = springSecurityService.getCurrentUser()
            if(user)
            {
                importedValue.put("initiatingSubscriber", "${user.id}");
            }
        }
        catch(MissingPropertyException e)
        {
            // handles a non-User principal
            if(log.isDebugEnabled())
            {
                log.debug 'MissingPropertyException while building SPOT request, probably an API user'
            }
        }

        importedValue.put("initiatingChannel", Channel.GUI.toString());

        Map<String, String> params = new TreeMap<String, String>();
        params.put("uri", "/interact/apps/iistart.ccxml");
//        def json = importedValue.encodeAsJSON()
        def theJson = importedValue as JSON
        String json = theJson.toString(false);
        params.put("II_SB_importedValue", json);
        sendRequest(params);
    }

    private void sendRequest(Map<String, String> params) throws IOException, SpotCommunicationException
    {
        List<String> spotUrls = new ArrayList<String>(1);
        if(grailsApplication.config.com.interact.listen.spotUrl != null &&
                !grailsApplication.config.com.interact.listen.spotUrl.isEmpty())
        {
            spotUrls.add(grailsApplication.config.com.interact.listen.spotUrl);
        }
        else
        {
            List<SpotSystem> spotSystems = SpotSystem.findAll();
            for(SpotSystem thisSystem : spotSystems)
            {
                spotUrls.add(thisSystem.name);
            }
        }

        sendRequest(params, spotUrls);
    }

    private void sendRequest(Map<String, String> params, List<String> spotUrls) throws IOException, SpotCommunicationException
    {
        if(log.isDebugEnabled())
        {
            log.debug "Sending SPOT HTTP request with params ${params} to ${spotUrls.size()} SPOT systems"
        }

        def failed = []
        int status = -1

        for(String thisSpotUrl : spotUrls)
        {
            def httpClient = new HttpClientImpl()

            String uri = thisSpotUrl + "/ccxml/createsession";
            httpClient.post(uri, params);

            status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status))
            {
                failed << it
            }
        }
        if(failed.size() > 0)
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System(s) at [" +
                    (failed.collect { it.uri }.join(',')) + "]", status);
        }

        if(log.isDebugEnabled())
        {
            log.debug "Completed sendRequest [${failed.size()}]"
        }
    }

    private boolean isSuccessStatus(int status) {
        return status >= 200 && status <= 299;
    }
} 
