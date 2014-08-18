package com.interact.listen.spot

import com.interact.listen.User
import com.interact.listen.history.Channel
import com.interact.listen.httpclient.HttpClientImpl
import com.interact.listen.stats.Stat
import grails.converters.JSON
import org.joda.time.format.DateTimeFormat

class SpotCommunicationService
{
    private static final String LISTEN_ORIGIN = "Listen_Controller";

    private static final String CUSTOM_EVENT = "customEvent";
    private static final String SESSION_ID = "sessionId";
    private static final String ORIGIN = "origin";
    private static final String ARGS = "args";
    private static final String INITIATING_SUBSCRIBER = "initiatingSubscriber";

    private static final String DROP_CALL = "DROP";
    private static final String MUTE_CALL = "MUTE";
    private static final String DIAL = "DIAL";
    private static final String INTERACTIVE_DIAL = "INTERACTIVE_DIAL";
    private static final String START_CONFERENCE = "START";
    private static final String STOP_CONFERENCE = "STOP";
    private static final String UNMUTE_PARTICIPANT = "UNMUTE";
    private static final String FAX = "PDF_TO_TIFF";
    private static final String ACD_CONNECT = "CONNECT";
    private static final String ACD_DISCONNECT = "DISCONNECT";
    private static final String ACD_VOICEMAIL = "VOICEMAIL";
    private static final String ACD_HOLD = "ON_HOLD";
    private static final String ACD_OFFHOLD = "OFF_HOLD";
    private static final String ACD_SWITCH_QUEUE = "SWITCH_QUEUE";

    static transactional = false

    def grailsApplication
    def springSecurityService
    def statWriterService

    def dropParticipant(def participant) throws IOException, SpotCommunicationException
    {
        sendConferenceParticipantEvent(DROP_CALL, participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_DROP);
    }

    def muteParticipant(def participant) throws IOException, SpotCommunicationException
    {
        sendConferenceParticipantEvent(MUTE_CALL, participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_MUTE)
    }

    def outdial(def numbers, def conference, def requestingNumber) throws IOException, SpotCommunicationException
    {
        Map<String, Object> args = new TreeMap<String, Object>();
        args.put("conferenceId", conference.id);
        args.put("destination", numbers);
        args.put("organization", "/organizations/${conference.owner.organization.id}")
        args.put("ani", requestingNumber);

        buildAndSendRequest(DIAL, conference.firstAdminSessionId(), args);
        statWriterService.send(Stat.SPOT_AUTO_DIAL_DIAL)
    }

    def interactiveOutdial(def numbers, def conference, def requestingNumber)
        throws IOException, SpotCommunicationException
    {
        Map<String, Object> args = new TreeMap<String, Object>();
        args.put("destination", numbers);
        args.put("ani", requestingNumber);
        args.put("organization", "/organizations/${conference.owner.organization.id}")

        buildAndSendRequest(INTERACTIVE_DIAL, conference.firstAdminSessionId(), args);
        statWriterService.send(Stat.SPOT_CONF_EVENT_BRIDGE_DIAL)
    }

    def startRecording(def conference) throws IOException, SpotCommunicationException
    {
        sendConferenceRecordingEvent(START_CONFERENCE, conference);
        statWriterService.send(Stat.SPOT_RECORD_START)
    }

    def stopRecording(def conference) throws IOException, SpotCommunicationException {
        sendConferenceRecordingEvent(STOP_CONFERENCE, conference);
        statWriterService.send(Stat.SPOT_RECORD_STOP)
    }

    def toggleMessageLight(def number, def ip, boolean on) throws IOException, SpotCommunicationException
    {
        sendMessageLightEvent(on ? 'ON' : 'OFF', number, ip)
        statWriterService.send(on ? Stat.SPOT_MSG_LIGHT_ON : Stat.SPOT_MSG_LIGHT_OFF)
    }

    def unmuteParticipant(def participant) throws IOException, SpotCommunicationException
    {
        sendConferenceParticipantEvent(UNMUTE_PARTICIPANT, participant);
        statWriterService.send(Stat.SPOT_CONF_EVENT_UNMUTE)
    }

    def sendConferenceParticipantEvent(def action, def participant) throws IOException, SpotCommunicationException 
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        buildAndSendRequest(action, participant.sessionId, importedValue);
    }

    def sendConferenceRecordingEvent(def action, def conference) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("conferenceId", conference.id);
        importedValue.put("startTime", DateTimeFormat.forPattern('yyyyMMddhhmmssSSS').print(conference.startTime));
        importedValue.put("recordingSessionId", conference.recordingSessionId);
        importedValue.put("arcadeId", conference.arcadeId);
        importedValue.put("description", conference.description);
        buildAndSendRequest(action, conference.firstAdminSessionId(), importedValue);
    }

    def sendFax(def fax) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("artifact", fax.sourceFiles.collect{ it.file.absolutePath });
        importedValue.put("destination", fax.dnis);
        importedValue.put("ani", ""); //Will be implemented later, probably once sold and we know what to do
        importedValue.put("organization", "/organizations/${fax.sender.organization.id}");
        importedValue.put("id", fax.id)
        buildAndSendRequest(FAX, null, importedValue);
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
        importedValue.put("number", number);

        buildAndSendRequest(ACD_CONNECT, sessionId, importedValue);
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
        buildAndSendRequest(ACD_DISCONNECT, sessionId, importedValue);
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
        buildAndSendRequest(event, sessionId, importedValue);
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
        importedValue.put("number", number);

        buildAndSendRequest(ACD_VOICEMAIL, sessionId, importedValue);
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
        buildAndSendRequest(ACD_HOLD, sessionId, importedValue);
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
        buildAndSendRequest(ACD_OFFHOLD, sessionId, importedValue);
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
        importedValue.put("onHoldMsg", onHoldMsg);
        importedValue.put("onHoldMusic", onHoldMusic);
        importedValue.put("connectMsg", connectMsg);
        importedValue.put("onHoldMsgExtended", onHoldMsgExtended);
        buildAndSendRequest(ACD_SWITCH_QUEUE, sessionId, importedValue);
    }


    private void sendMessageLightEvent(def action, def number, def ip) throws IOException, SpotCommunicationException
    {
        Map<String, Object> importedValue = new TreeMap<String, Object>();
        importedValue.put("destination", number);
        importedValue.put('ip', ip)
        buildAndSendRequest(action, null, importedValue);
    }

    private void buildAndSendRequest(String event, String sessionId, Map<String, Object> args) throws IOException, SpotCommunicationException
    {
        Map<String, String> params = new TreeMap<String, String>();

        try
        {
            User user = (User)springSecurityService.getCurrentUser()
            if(user)
            {
                params.put(INITIATING_SUBSCRIBER, String.valueOf(user.id));
            }
        }
        catch(MissingPropertyException e)
        {
            // handles a non-User principal
            if(log.isDebugEnabled())
            {
                log.debug("MissingPropertyException[" + e + "] while building SPOT request, probably an API user");
            }
        }

        params.put(CUSTOM_EVENT, event);
        if(sessionId != null)
        {
            params.put(SESSION_ID, sessionId);
        }
        params.put(ORIGIN, LISTEN_ORIGIN);
        def argsJson = args as JSON
        String json = argsJson.toString(false);
        params.put(ARGS, json);
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

    private void sendRequest(Map<String, String> params, List<String> spotUrls)
        throws IOException, SpotCommunicationException
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

//            httpClient.post(thisSpotUrl, params);
            httpClient.get(thisSpotUrl, params);

            status = httpClient.getResponseStatus();
            if(!isSuccessStatus(status))
            {
                failed << thisSpotUrl;
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

    private boolean isSuccessStatus(int status)
    {
        return status >= 200 && status <= 299;
    }
}

