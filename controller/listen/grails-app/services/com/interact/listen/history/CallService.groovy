package com.interact.listen.history

import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.acd.AcdCallHistory
import com.interact.listen.acd.AcdCallStatus
import com.interact.listen.exceptions.ListenExportException
import grails.validation.ValidationErrors
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.codehaus.groovy.grails.web.util.WebUtils
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

class CallService {
    def grailsApplication

    public void exportCallHistoryToCSV(Organization organization, org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap params) throws ListenExportException
    {
        def users = User.findAllByOrganization(organization)
        def listen = grailsApplication.mainContext.getBean('com.interact.listen.DateTimeTagLib');

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        def startDate
        def endDate
        if (params.startDate) {
            startDate = getStartDate(params.startDate)
        }

        if (params.endDate) {
            endDate = getEndDate(params.endDate)
        }

        def selectedUsers
        if (params.user) {
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect{ Long.valueOf(it)}, organization)
        }

        File tmpFile
        try {
            log.debug("Creating temp file to extract for action history [${params}]")
            tmpFile = File.createTempFile("./listen-callhistory-${new LocalDateTime().toString('yyyyMMddHHmmss')}", ".csv")
            tmpFile.deleteOnExit()
            log.debug("tmpFile [${tmpFile.getName()}] created.")
        } catch (IOException e) {
            log.error("Failed to create temp file for export: ${e}")
            flash.errorMessage = message(code: 'callHistory.exportCSV.fileCreateFail')
            redirect(action: "callHistory", params: params)
            return
        }

        log.debug("Pulling records")

        def callHistory = CallHistory.createCriteria().list([sort: 'dateTime', order: 'desc']) {
            if (startDate && endDate) {
                and {
                    ge('dateTime', startDate)
                    le('dateTime', endDate)
                }
            } else if (startDate) {
                ge('dateTime', startDate)
            } else if (endDate) {
                le('dateTime', endDate)
            }

            if (params.caller) {
                ilike('ani', '%'+params.caller.replaceAll("\\D+", "")+'%')
            }

            if (params.callee) {
                ilike('dnis', '%'+params.callee.replaceAll("\\D+", "")+'%')
            }

            if (params.inboundDnis && params.searchButton) {
                ilike('inboundDnis', '%'+params.inboundDnis.replaceAll("\\D+", "")+'%')
            }

            if (selectedUsers) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult) {
                ilike('result', '%'+params.callResult+'%')
            }

            eq('organization', organization)
        }

        // Build the data now
        try {
            //Create header row
            tmpFile << "timestamp,began,calling party,caller id,called party,dialed number,duration,organization,call result,sessionId,common call id,ivr,"
            tmpFile << AcdCallHistory.csvHeader();  // we are adding acd history to the export header
            tmpFile << "\n";

            callHistory.each {
                def acdCallHist = AcdCallHistory.findAllBySessionIdAndCallStatusNotEqual(it?.sessionId, AcdCallStatus.TRANSFER_REQUESTED)
                if (acdCallHist) {
                    log.debug("Found acd call history records for session id [${it.sessionId}]")
                    acdCallHist.each { acdCall ->
                        log.debug("Exporting call and acd history for session id [${it.sessionId}]")
                        // first we'll add the call history domain
                        tmpFile << "${it.dateTime?.getMillis()},"
                        tmpFile << "${it.dateTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${listen.numberWithRealName(number: it.ani, user: it.fromUser, personalize: false)},"
                        tmpFile << "${it.outboundAni},"
                        tmpFile << "${listen.numberWithRealName(number: it.dnis, user: it.toUser, personalize: false)},"
                        tmpFile << "${it.inboundDnis},"
                        tmpFile << "${listen.formatduration(duration: it.duration, millis: false)},"
                        tmpFile << "${it.organization.name},"
                        tmpFile << "${it.result.replaceAll(",", " ")}," // This is to prevent anything weird...
                        tmpFile << "${it.sessionId},"
                        tmpFile << "${it.commonCallId},"
                        tmpFile << "${it.ivr},"
                        // Now we'll add the acd call domain portion
                        tmpFile << "${acdCall.skill},"
                        tmpFile << "${acdCall.enqueueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${acdCall.dequeueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${listen.computeDuration(start: acdCall.enqueueTime, end: acdCall.dequeueTime)},"
                        tmpFile << "${acdCall.callStatus.name()},"
                        if(acdCall.user != null)
                        {
                            tmpFile << "${acdCall.user.username},"
                        }
                        else
                        {
                            tmpFile << ","
                        }
                        tmpFile << "${acdCall.agentCallStart?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${acdCall.agentCallEnd?.toString("yyyy-MM-dd HH:mm:ss")},"
                        if(acdCall.agentCallEnd != null && acdCall.agentCallEnd != null)
                        {
                            tmpFile << "${listen.computeDuration(start: acdCall.agentCallStart, end: acdCall.agentCallEnd)},"
                        }
                        else
                        {
                            //need a zero duration then
                            DateTime now = DateTime.now();
                            tmpFile << "${listen.computeDuration(start: now, end: now)},"
                        }

                        tmpFile << "\n"
                    }
                } else {
                    // we don't have any acd call history, so lets just output the call history domain
                    log.debug("Exporting call history for session id [${it.sessionId}]")
                    tmpFile << "${it.dateTime?.getMillis()},"
                    tmpFile << "${it.dateTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                    tmpFile << "${listen.numberWithRealName(number: it.ani, user: it.fromUser, personalize: false)},"
                    tmpFile << "${it.outboundAni},"
                    tmpFile << "${listen.numberWithRealName(number: it.dnis, user: it.toUser, personalize: false)},"
                    tmpFile << "${it.inboundDnis},"
                    tmpFile << "${listen.formatduration(duration: it.duration, millis: false)},"
                    tmpFile << "${it.organization.name},"
                    tmpFile << "${it.result.replaceAll(",", " ")}," // This is to prevent anything weird...
                    tmpFile << "${it.sessionId},"
                    tmpFile << "${it.commonCallId},"
                    tmpFile << "${it.ivr},"
                    tmpFile << "\n"
                }
            }
        } catch (Exception e) {
            log.error("Exception building csv file: ${e}")
            throw new ListenExportException("Unable to export csv");
        }

        def filename = "listen-callhistory-${new LocalDateTime().toString('yyyyMMddHHmmss')}.csv"
        def utils = WebUtils.retrieveGrailsWebRequest()
        def response = utils.getCurrentResponse()

        response.contentType = "text/csv"
        response.setHeader("Content-disposition", "attachment;filename=${filename}")
        response.setHeader("Content-length", "${tmpFile.length()}")

        OutputStream outputStream = new BufferedOutputStream(response.outputStream)
        InputStream inputStream = tmpFile.newInputStream()

        byte[] bytes = new byte[4096]
        int bytesRead;

        while ((bytesRead = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, bytesRead)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()
        response.flushBuffer()

        if (tmpFile.delete() == false) {
            log.error("Failed to delete temp file [${tmpFile.getName()}]")
            return
        }

        log.debug("temp file deleted")
        return
    }

    /**
     * Return the ivr for this controller
     * @return The Ivr.
     */
    public String getIvr()
    {
        String returnVal = null;

        if(grailsApplication.config.com.interact.listen.ivr != null &&
                !grailsApplication.config.com.interact.listen.ivr.isEmpty())
        {
            returnVal = grailsApplication.config.com.interact.listen.ivr;
        }

        return returnVal;
    }

    private def getStartDate(String inputStart)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theStart;

        if(inputStart && !inputStart.isEmpty())
        {
            theStart = DateTime.parse(inputStart, dtf);
        }
        else
        {
            theStart = DateTime.now().minusDays(1);
        }

        theStart = theStart.withHourOfDay(0);
        theStart = theStart.withMinuteOfHour(0);
        theStart = theStart.withSecondOfMinute(0);

        return theStart;
    }

    private def getEndDate(String inputEnd)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theEnd;

        if(inputEnd && !inputEnd.isEmpty())
        {
            theEnd = DateTime.parse(inputEnd, dtf);
        }
        else
        {
            theEnd = DateTime.now();
        }

        theEnd = theEnd.withHourOfDay(23);
        theEnd = theEnd.withMinuteOfHour(59);
        theEnd = theEnd.withSecondOfMinute(59);

        return theEnd;
    }

}
