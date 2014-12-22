package com.interact.listen.spot

import com.interact.listen.User
import com.interact.listen.history.Channel
import com.interact.listen.httpclient.HttpClientImpl
import com.interact.listen.stats.Stat
import grails.converters.JSON
import org.joda.time.format.DateTimeFormat
import java.net.URLEncoder;

class SpotCommunicationService {
    static transactional = false

    def grailsApplication
    def springSecurityService
    def statWriterService

    def dropParticipant(def participant) throws IOException, SpotCommunicationException {
        def success = false
        try {
            log.debug("Attempting to drop participant [${participant.id}]")
            sendConferenceParticipantEvent("LISTEN_CONF_DROP", participant);
            statWriterService.send(Stat.SPOT_CONF_EVENT_DROP)
            success = true
        } catch (Exception e) {
            log.error("We've encounted an error attempting to drop participant [${participant.id}][${e}]")
        }
        return success
    }

    def muteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("LISTEN_CONF_MUTE", participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_MUTE)
    }

    def outdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "AUTO_DIAL");
        importedValue.put("customEvent", "DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("conferenceId", conference.id);
        importedValue.put("destination", numbers);
        importedValue.put("organization", "/organizations/${conference.owner.organization.id}")
        importedValue.put("ani", requestingNumber);
        buildAndSendVexRequest(importedValue);
        statWriterService.send(Stat.SPOT_AUTO_DIAL_DIAL)
    }

    def interactiveOutdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("customEvent", "INTERACTIVE_DIAL");
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("destination", numbers);
        importedValue.put("ani", requestingNumber);
        importedValue.put("organization", "/organizations/${conference.owner.organization.id}")
        buildAndSendVexRequest(importedValue);
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
        sendMessageLightEvent(on ? 'ON' : 'OFF', number, ip);
        statWriterService.send(on ? Stat.SPOT_MSG_LIGHT_ON : Stat.SPOT_MSG_LIGHT_OFF)
    }

    def unmuteParticipant(def participant) throws IOException, SpotCommunicationException {
        sendConferenceParticipantEvent("LISTEN_CONF_UNMUTE", participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_UNMUTE)
    }

    def sendConferenceParticipantEvent(def customEvent, def participant) throws IOException, SpotCommunicationException {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "CONF_EVENT");
        importedValue.put("customEvent", customEvent);
        importedValue.put("sessionId", participant.sessionId);
        buildAndSendVexRequest(importedValue);
    }

    def sendConferenceRecordingEvent(def customEvent, def conference) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "RECORD");
        importedValue.put("customEvent", customEvent);
        importedValue.put("sessionId", conference.firstAdminSessionId());
        importedValue.put("conferenceId", conference.id);
        importedValue.put("startTime", DateTimeFormat.forPattern('yyyyMMddhhmmssSSS').print(conference.startTime));
        importedValue.put("recordingSessionId", conference.recordingSessionId);
        importedValue.put("arcadeId", conference.arcadeId);
        importedValue.put("description", conference.description);
        buildAndSendVexRequest(importedValue);
    }

    def sendFax(def fax) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("application", "Direct Message");
        importedValue.put("customEvent", "PDF_TO_TIFF");
        importedValue.put("artifact", fax.sourceFiles.collect{ it.file.absolutePath });
        importedValue.put("destination", fax.dnis);
        importedValue.put("ani", ""); //Will be implemented later, probably once sold and we know what to do
        importedValue.put("organization", "/organizations/${fax.sender.organization.id}");
        importedValue.put("id", fax.id)
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "CONNECT");
        importedValue.put("sessionId", sessionId.toString());
        importedValue.put("number", number.toString());
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "DISCONNECT");
        importedValue.put("sessionId", sessionId.toString());
        buildAndSendVexRequest(importedValue);
    }

    /**
     * Send request to SPOT with a generic event
     *
     * @param sessionId The sessionId of the call.
     * @param event The customEvent to use
     * @throws IOException If an IOException.
     * @throws SpotCommunicationException If
     */
    def sendAcdGenericEvent(def sessionId, def event) throws IOException, SpotCommunicationException
    {
        if(log.isInfoEnabled())
        {
            log.info("Sending AcdGenericEvent, sessionId[" + sessionId + "], event[" + event + "]");
        }

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", event.toString());
        importedValue.put("sessionId", sessionId.toString());
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "VOICEMAIL");
        importedValue.put("sessionId", sessionId.toString());
        importedValue.put("number", number.toString());
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "ON_HOLD");
        importedValue.put("sessionId", sessionId.toString());
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "OFF_HOLD");
        importedValue.put("sessionId", sessionId.toString());
        buildAndSendVexRequest(importedValue);
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

        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("application", "ACD");
        importedValue.put("customEvent", "SWITCH_QUEUE");
        importedValue.put("sessionId", sessionId.toString());
        importedValue.put("onHoldMsg", onHoldMsg.toString());
        importedValue.put("onHoldMusic", onHoldMusic.toString());
        importedValue.put("connectMsg", connectMsg.toString());
        importedValue.put("onHoldMsgExtended", onHoldMsgExtended.toString());
        buildAndSendVexRequest(importedValue);
    }


    private void sendMessageLightEvent(def action, def number, def ip) throws IOException, SpotCommunicationException {
        Map<String, String> importedValue = new TreeMap<String, String>();
        importedValue.put("app", "MSG_LIGHT");
        importedValue.put("action", action);
        importedValue.put("destination", number);
        importedValue.put('customEvent', 'event.user.messagelightcontrol')
        importedValue.put('uri', 'file:///interact/apps/iistart.ccxml')
        importedValue.put("destination", number.toString());
        log.debug "'send message light event [' + importedValue + ']'"
        buildAndSendSpotRequest(importedValue);
    }

    private void buildAndSendSpotRequest(Map<String, String> importedValue) throws IOException, SpotCommunicationException
    {
        try
        {
            def user = springSecurityService.getCurrentUser()
            if(user)
            {
                importedValue.put("initiatingSubscriber", user.id.toString());
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

        sendSpotRequest(importedValue);
    }

    private void sendSpotRequest(Map<String, String> params) throws IOException, SpotCommunicationException
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

        sendSpotRequest(params, spotUrls);
    }

    private void sendSpotRequest(Map<String, String> params, List<String> spotUrls) throws IOException, SpotCommunicationException
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

            String uri = thisSpotUrl + "/spot/ccxml/createsession";
            log.debug "Sending SPOT HTTP request to [${uri}]"
            httpClient.post(uri, params);

            status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status))
            {
                failed << thisSpotUrl
            }
        }
        if(failed.size() > 0)
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from SPOT System(s) at [" +
                    (failed.join(',')) + "]", status);
        }

        if(log.isDebugEnabled())
        {
            log.debug "Completed sendRequest [${failed.size()}]"
        }
    }

    private void buildAndSendVexRequest(Map<String, String> importedValue) throws IOException, SpotCommunicationException
    {
        try
        {
            def user = springSecurityService.getCurrentUser()
            if(user)
            {
                importedValue.put("initiatingSubscriber", user.id.toString());
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

        sendVexRequest(importedValue);
    }

    private void sendVexRequest(Map<String, String> params) throws IOException, SpotCommunicationException
    {
        List<String> vexUrls = new ArrayList<String>(1);
        if(grailsApplication.config.com.interact.listen.vexUrl != null &&
                !grailsApplication.config.com.interact.listen.vexUrl.isEmpty())
        {
            vexUrls.add(grailsApplication.config.com.interact.listen.vexUrl);
        } else {
            log.error "No vexUrl has been configured"
        }

        sendVexRequest(params, vexUrls);
    }

    private void sendVexRequest(Map<String, String> params, List<String> vexUrls) throws IOException, SpotCommunicationException
    {
        if(log.isDebugEnabled())
        {
            log.debug "Sending Vex HTTP request with params ${params} to ${vexUrls.size()} SPOT systems"
        }

        def failed = []
        int status = -1

        log.debug "Sending to Vex with params [${params}]"

        for(String thisVexUrl : vexUrls)
        {
            def httpClient = new HttpClientImpl()

            String base = thisVexUrl + "/customEvent?sessionId=" + URLEncoder.encode(params.get('sessionId'), "UTF-8") + "&customEvent=" + URLEncoder.encode(params.get('customEvent'), "UTF-8") + "&";
            log.debug "Sending VEX HTTP request to [${base}]"
            /*
            * sessionId
            * customEvent (was "action" before)
            * args = what we want to send...
            * 
            */
            // create a JSON object from the map...
            def theJson = params as JSON
            String json = theJson.toString(false);
            
            def url = base + "args=" + URLEncoder.encode(json, "UTF-8");
            httpClient.get(url);

            status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status))
            {
                failed << thisVexUrl
            }
        }
        if(failed.size() > 0)
        {
            throw new SpotCommunicationException("Received HTTP Status " + status + " from Vex System(s) at [" +
                    (failed.join(',')) + "]", status);
        }

        if(log.isDebugEnabled())
        {
            log.debug "Completed sendVexRequest [${failed.size()}]"
        }
    }

    private boolean isSuccessStatus(int status) {
        return status >= 200 && status <= 299;
    }
} 
