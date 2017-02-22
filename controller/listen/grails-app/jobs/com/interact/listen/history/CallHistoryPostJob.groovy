package com.interact.listen.history

import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdCallStatus
import org.apache.http.conn.HttpHostConnectException
import com.interact.listen.acd.AcdCallHistory

import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.params.HttpConnectionParams
import com.interact.listen.stats.*

import grails.converters.JSON
import grails.util.Holders

import org.joda.time.*

import java.io.IOException

class CallHistoryPostJob {
	private static final Integer HTTP_CONNECTION_TIMEOUT = 5000
	private static final Integer HTTP_SOCKET_TIMEOUT = 5000

	static triggers = {
		cron name: 'CallHistoryPostTrigger', cronExpression: '0 0/1 * * * ?'
	}

	def concurrent = false;
	def statWriterService

	def execute() {
		// get our call records
		// callRecords = CallHistory.findByCdrPostResult(null)
		// get all call history records that don't currently have a 200 result for cdr post
        log.debug("Running Call History Post Job");
        def activeAcdCommonCallIds = AcdCall.getAll().collect { it.commonCallId }
        log.debug("activeAcdCommonCallIds is ${activeAcdCommonCallIds}");

		def callRecords = CallHistory.createCriteria().list {
			ne('cdrPostResult', 200)
			lt('cdrPostCount', 3)
			gt('dateTime', new LocalDate().toDateTimeAtCurrentTime().minusDays(Integer.parseInt((String) Holders.config.com.interact.listen.history.postRange)))
            if (activeAcdCommonCallIds.size() > 0) {
                not {
                    'in'('commonCallId', activeAcdCommonCallIds)
                }
            }
		}

        log.debug("Number of call Records: ${callRecords.size()}");
        def processedCallRecordCommonCallIds = []

		callRecords.each { callRecord ->
			// only do the post if it's configured for the organization
			if (callRecord.organization.postCdr && callRecord.organization.cdrUrl && !(callRecord.commonCallId in processedCallRecordCommonCallIds)) {
                def recordList = []
                def acdCallRecords = AcdCallHistory.createCriteria().list {
                    eq('commonCallId', callRecord.commonCallId)
                }
                recordList.addAll(generateList(callRecord, acdCallRecords))

                // Find all CDR's with commonCallId the same and add them
                def associatedCallRecords = getAssociatedCallRecords(callRecord, callRecords)

                // Loop through and continue building the jsonArr
                associatedCallRecords.each { associatedCallRecord ->
                    // Assuming same common call id means all ACD call records are pulled for the original call
                    // So we just add this call record to the list and call it good
                    recordList.addAll(generateList(associatedCallRecord, null))
                }

                processedCallRecordCommonCallIds.add(callRecord.commonCallId)

                def statusCode = sendRequest(callRecord.organization.cdrUrl, recordList)
                def result = updateCallRecords(callRecord, associatedCallRecords, statusCode)

				statWriterService.send(result.toString().charAt(0) == "2" ? Stat.SPOT_POST_CDR_SUCCESS : Stat.SPOT_POST_CDR_FAILURE)
			}
		}
	}

    def getAssociatedCallRecords(def currentCallRecord, def callRecords) {
        // Check if there are any other CDR's with the commonCallId
        return callRecords.findAll {
            if (it.commonCallId == currentCallRecord.commonCallId && it != currentCallRecord) {
                return it
            }
        }
    }

    def sendRequest(def url, def recordList)
    {
        // set up our http client
        HttpClient client = new DefaultHttpClient()
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, HTTP_CONNECTION_TIMEOUT)
        client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, HTTP_SOCKET_TIMEOUT)

        String postUrl = url;
        log.debug("Post URL: " + postUrl);

        String headerArgument = null;
        String[] splits = postUrl.split(" ");
        if(splits.size() > 1)
        {
            //We probably got a parameterized URL configuration
            if(splits[1].equalsIgnoreCase("-H") && splits.size() > 2)
            {
                //the header params could be surrounded by " or spaces
                if(splits[2].startsWith("\""))
                {
                    if(splits[2].endsWith("\""))
                    {
                        headerArgument = splits[2].replaceAll("\"", "");
                    }
                    else if(splits.size() > 3)
                    {
                        headerArgument = (splits[2] + " " + splits[3]).replaceAll("\"", "");
                    }
                    else
                    {
                        headerArgument = splits[2].replaceAll("\"", "");
                    }
                }
                else
                {
                    headerArgument = splits[2].replaceAll("\"", "");
                }

                log.debug("Received URL header argument: " + headerArgument);
            }

            url = splits[0];
        }

        HttpPost post = new HttpPost(url);

        if(headerArgument != null && !headerArgument.isEmpty())
        {
            //Split the arg and value on :
            String[] headerArgs = headerArgument.split(":");
            if(headerArgs.size() == 2)
            {
                post.addHeader(headerArgs[0], headerArgs[1]);
            }
        }

        post.addHeader("content-type", "application/json; charset=utf-8")
        def json = (recordList as JSON)
        post.setEntity(new StringEntity("${json}"))
        log.debug "body [${json}]"

        int statusCode = 0

        try {
            HttpResponse response = client.execute(post)
            statusCode = response.getStatusLine().getStatusCode()
        }
        catch (ConnectTimeoutException e) {
            statusCode = 504
            log.debug "Connection timeout occurred updating post result details for call record."
        }
        catch (SocketTimeoutException e) {
            statusCode = 504
            log.debug "Socket timeout occurred updating post result details for call record."
        }
        catch (HttpHostConnectException hhce) {
            statusCode = 504;
            log.warn("HttpHostConnectionException occurred updating post result details for call record : " + hhce);
        }
        catch (UnknownHostException uhe) {
            statusCode = 504;
            log.warn("UnknownHostException occurred updating post result details for call record : " + uhe);
        }
        catch (Exception e) {
            statusCode = 500
            log.warn("Internal Server Error [${e}] occurred updating  result details for call record.");
        }

        return statusCode
    }

    def updateCallRecords(def callRecord, def associatedCallRecords, def statusCode) {
        def success = true

        callRecord.cdrPostResult = statusCode
        callRecord.cdrPostCount++
        if (!callRecord.save()) {
            log.debug "Failed to update call record post result details for call history [${callRecord.id}]"
            success = false
        }

        associatedCallRecords.each {
            it.cdrPostResult = statusCode
            it.cdrPostCount++
            if (!it.save()) {
                log.debug "Failed to update call record post result details for call history [${callRecord.id}]"
                success = false
            }
        }

        return (success ? statusCode : 500)
    }

	def generateList(def callRecord, def acdCallRecords) {
		def jsonArr = []

		if (acdCallRecords != null && acdCallRecords.size() > 0)
        {
			acdCallRecords.each { record ->
                if (record.callStatus == AcdCallStatus) {
                    log.debug("Ignoring TRANSFER_REQUESTED acd history entry");
                    return
                }

				def json = [:]
				json.sessionId = callRecord.sessionId
                DateTime callReceivedUtc = callRecord.dateTime?.withZone(DateTimeZone.UTC);
				json.callReceived = callReceivedUtc?.toString("yyyy-MM-dd HH:mm:ss")
				json.timeStamp = "${callRecord.dateTime.getMillis().toString()}"
                json.commonCallId = callRecord.commonCallId
                json.callerId = callRecord.outboundAni
                json.dialedNumber = callRecord.inboundDnis
				json.ani = callRecord.ani
				json.dnis = callRecord.dnis
				json.agent = record.agentNumber ?: null
                DateTime enqueueTimeUtc = record.enqueueTime?.withZone(DateTimeZone.UTC);
				json.enqueueTime = enqueueTimeUtc?.toString("yyyy-MM-dd HH:mm:ss") ?: null;
                DateTime agentCallStartUtc = record.agentCallStart?.withZone(DateTimeZone.UTC);
				json.agentCallStart = agentCallStartUtc?.toString("yyyy-MM-dd HH:mm:ss") ?: null
                DateTime agentCallEndUtc = record.agentCallEnd?.withZone(DateTimeZone.UTC);
				json.agentCallEnd = agentCallEndUtc?.toString("yyyy-MM-dd HH:mm:ss") ?: null
				jsonArr.push(json)
			}
		}
        else
        {
			def json = [:]
			json.sessionId = callRecord.sessionId
            DateTime callReceivedUtc = callRecord.dateTime?.withZone(DateTimeZone.UTC);
            json.callReceived = callReceivedUtc?.toString("yyyy-MM-dd HH:mm:ss")
			json.timeStamp = "${callRecord.dateTime.getMillis().toString()}"
            json.commonCallId = callRecord.commonCallId
            json.callerId = callRecord.outboundAni
            json.dialedNumber = callRecord.inboundDnis
			json.ani = callRecord.ani
			json.dnis = callRecord.dnis
			json.agent = null
			json.enqueueTime = null
			json.agentCallStart = null
			json.agentCallEnd = null

			jsonArr.push(json)
		}

		return jsonArr
	}
}
