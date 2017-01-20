package com.interact.listen.history

import com.google.gson.JsonNull
import org.apache.http.conn.HttpHostConnectException
import org.joda.time.LocalDateTime
import com.interact.listen.PrimaryNode
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
    def callRecords = CallHistory.createCriteria().list {
      ne('cdrPostResult', 200)
      lt('cdrPostCount', 3)
      gt('dateTime', new LocalDate().toDateTimeAtCurrentTime().minusDays(Integer.parseInt((String) Holders.config.com.interact.listen.history.postRange)))
    }

    callRecords.each { callRecord ->
      // set up our http client
      HttpClient client = new DefaultHttpClient()
      client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, HTTP_CONNECTION_TIMEOUT)
      client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, HTTP_SOCKET_TIMEOUT)

      def acdCallRecords = AcdCallHistory.findAllBySessionId(callRecord.sessionId)
      def url = callRecord.organization.cdrUrl
      // only do the post if it's configured for the organization
      if (callRecord.organization.postCdr && callRecord.organization.cdrUrl) {
        HttpPost post = new HttpPost(url)
        def json = generateJson(callRecord, acdCallRecords)
        post.setEntity(new StringEntity("${json}"))
        log.debug "body[${json}]"

        post.setEntity(new StringEntity("${json as JSON}"))
        log.debug "body[${json as JSON}]"

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
        catch (HttpHostConnectException hhce)
        {
            statusCode = 504;
            log.warn("HttpHostConnectionException occurred updating post result details for call record : " + hhce);
        }
        catch (UnknownHostException uhe)
        {
            statusCode = 504;
            log.warn("UnknownHostException occurred updating post result details for call record : " + uhe);
        }
        catch (Exception e) {
          statusCode = 500
          log.warn("Internal Server Error [${e}] occurred updating  result details for call record.");
        }

        callRecord.cdrPostResult = statusCode
        callRecord.cdrPostCount++

        if (!callRecord.save(flush: true)) {
          log.debug "Failed to update call record post result details for call history [${callRecord.id}]"
          statusCode = 500
        }

        statWriterService.send(statusCode.toString().charAt(0) == "2" ? Stat.SPOT_POST_CDR_SUCCESS : Stat.SPOT_POST_CDR_FAILURE)
      }
    }
  }

  def generateJson(def callRecord, def acdCallRecords)
  {
    def jsonArr = []

    if (acdCallRecords.size() > 0)
    {
      acdCallRecords.each { record ->
        def json = [:]
        json.sessionId = callRecord.sessionId
        json.callReceived = callRecord.dateTime?.toString("yyyy-MM-dd HH:mm:ss")
        json.timeStamp = callRecord.dateTime.getMillis()
        json.ani = callRecord.ani
        json.dnis = callRecord.dnis
        json.agent = record.agentNumber ?: null
        json.enqueueTime = record.enqueueTime ?: null
        json.agentCallStart = record.agentCallStart ?: null
        json.agentCallEnd = record.agentCallEnd ?: null
        jsonArr.push(json)
      }
    }
    else
    {
      def json = [:]
      json.sessionId = callRecord.sessionId
      json.callReceived = callRecord.dateTime?.toString("yyyy-MM-dd HH:mm:ss")
      json.timeStamp = callRecord.dateTime.getMillis()
      json.ani = callRecord.ani
      json.dnis = callRecord.dnis
      json.agent = null
      json.enqueueTime = null
      json.agentCallStart = null
      json.agentCallEnd = null

      jsonArr.push(json)
    }

    return (jsonArr as JSON)
  }
}
