package com.interact.listen.history

import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.acd.AcdCallHistory
import com.interact.listen.acd.AcdCallStatus
import com.interact.listen.exceptions.ListenExportException
import grails.validation.ValidationErrors
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.codehaus.groovy.grails.web.util.WebUtils
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.context.MessageSource

class CallService {
    def grailsApplication
    MessageSource messageSource

    public void exportCallHistoryToCSV(Organization organization, org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap params) throws ListenExportException {
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
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect {
                Long.valueOf(it)
            }, organization)
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
                ilike('ani', '%' + params.caller.replaceAll("\\D+", "") + '%')
            }

            if (params.callee) {
                ilike('dnis', '%' + params.callee.replaceAll("\\D+", "") + '%')
            }

            if (params.inboundDnis && params.searchButton) {
                ilike('inboundDnis', '%' + params.inboundDnis.replaceAll("\\D+", "") + '%')
            }

            if (selectedUsers) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult) {
                ilike('result', '%' + params.callResult + '%')
            }

            eq('organization', organization)
        }

        // Build the data now
        try {
            //Create header row for the call history information
            //tmpFile << "timestamp,began,calling party,caller id,called party,dialed number,duration,organization,call result,sessionId,common call id,ivr,"
            tmpFile.append(messageSource.getMessage('callHistory.timeStamp.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.dateTime.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.ani.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.outboundAni.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.dnis.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.inboundDnis.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.duration.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.organization.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.callResult.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.sessionId.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.commonCallId.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('callHistory.ivr.label', null, null) + ",");

            // now append the header for the acd call histories
            tmpFile.append(messageSource.getMessage('acdCallHistory.skill.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.enqueueTime.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.dequeueTime.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.totalQueueTime.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.callStatus.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.user.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.agentCallStart.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.agentCallEnd.label', null, null) + ",");
            tmpFile.append(messageSource.getMessage('acdCallHistory.totalAgentTime.label', null, null) + ",");

            tmpFile << "\n";

            callHistory.each {
                def acdCallHist = AcdCallHistory.findAllBySessionIdAndUser(it?.sessionId, it.toUser)
                if (acdCallHist) {
                    log.debug("Found acd call history records for session id [${it.sessionId}]")
                    acdCallHist.each { acdCall ->
                        log.debug("Exporting call and acd history for session id [${it.sessionId}]")
                        // first we'll add the call history domain
                        tmpFile << "${it.dateTime?.getMillis()},"
                        tmpFile << "${it.dateTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${numberWithRealName(number: it.ani, user: it.fromUser)},"
                        tmpFile << "${it.outboundAni},"
                        tmpFile << "${numberWithRealName(number: it.dnis, user: it.toUser)},"
                        tmpFile << "${it.inboundDnis},"
                        tmpFile << "${formatduration(duration: it.duration, millis: false)},"
                        tmpFile << "${it.organization.name},"
                        tmpFile << "${it.result.replaceAll(",", " ")}," // This is to prevent anything weird...
                        tmpFile << "${it.sessionId},"
                        tmpFile << "${it.commonCallId},"
                        tmpFile << "${it.ivr},"
                        // Now we'll add the acd call domain portion
                        tmpFile << "${acdCall.skill},"
                        tmpFile << "${acdCall.enqueueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${acdCall.dequeueTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${computeDuration(start: acdCall.enqueueTime, end: acdCall.dequeueTime)},"
                        tmpFile << "${acdCall.callStatus.name()},"
                        if (acdCall.user != null) {
                            tmpFile << "${acdCall.user.username},"
                        } else {
                            tmpFile << ","
                        }
                        tmpFile << "${acdCall.agentCallStart?.toString("yyyy-MM-dd HH:mm:ss")},"
                        tmpFile << "${acdCall.agentCallEnd?.toString("yyyy-MM-dd HH:mm:ss")},"
                        if (acdCall.agentCallEnd != null && acdCall.agentCallEnd != null) {
                            tmpFile << "${computeDuration(start: acdCall.agentCallStart, end: acdCall.agentCallEnd)},"
                        } else {
                            //need a zero duration then
                            DateTime now = DateTime.now();
                            tmpFile << "${computeDuration(start: now, end: now)},"
                        }

                        tmpFile << "\n"
                    }
                } else {
                    // we don't have any acd call history, so lets just output the call history domain
                    log.debug("Exporting call history for session id [${it.sessionId}]")
                    tmpFile << "${it.dateTime?.getMillis()},"
                    tmpFile << "${it.dateTime?.toString("yyyy-MM-dd HH:mm:ss")},"
                    tmpFile << "${numberWithRealName(number: it.ani, user: it.fromUser)},"
                    tmpFile << "${it.outboundAni},"
                    tmpFile << "${numberWithRealName(number: it.dnis, user: it.toUser)},"
                    tmpFile << "${it.inboundDnis},"
                    tmpFile << "${formatduration(duration: it.duration, millis: false)},"
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
    public String getIvr() {
        String returnVal = null;

        if (grailsApplication.config.com.interact.listen.ivr != null &&
                !grailsApplication.config.com.interact.listen.ivr.isEmpty()) {
            returnVal = grailsApplication.config.com.interact.listen.ivr;
        }

        return returnVal;
    }

    private def getStartDate(String inputStart) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theStart;

        if (inputStart && !inputStart.isEmpty()) {
            theStart = DateTime.parse(inputStart, dtf);
        } else {
            theStart = DateTime.now().minusDays(1);
        }

        theStart = theStart.withHourOfDay(0);
        theStart = theStart.withMinuteOfHour(0);
        theStart = theStart.withSecondOfMinute(0);

        return theStart;
    }

    private def getEndDate(String inputEnd) {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theEnd;

        if (inputEnd && !inputEnd.isEmpty()) {
            theEnd = DateTime.parse(inputEnd, dtf);
        } else {
            theEnd = DateTime.now();
        }

        theEnd = theEnd.withHourOfDay(23);
        theEnd = theEnd.withMinuteOfHour(59);
        theEnd = theEnd.withSecondOfMinute(59);

        return theEnd;
    }

    public def computeDuration = { attrs ->
        if((!attrs.start) && (!attrs.end)) {
            return "0.00:00"
        }
        if(!attrs.start) {
            log.error('computeduration is missing required attribute [start]')
            return "0.00:00"
        }

        if(!attrs.end) {
            log.error('computeduration is missing required attribute [end]')
            return "0.00:00"
        }

        PeriodFormatter fmt = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(1)
                .appendHours()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
                .toFormatter();

        return fmt.print(new Period(attrs.start, attrs.end))
    }

    public def formatduration = { attrs ->
        if (!attrs.duration) {
            log.error('formatduration is missing required attribute [duration]')
            return '0.0';
        }

        boolean millis = attrs.containsKey('millis') ? Boolean.valueOf(attrs.millis) : false

        Duration tmpDuration = attrs.duration
        def milliSecs = tmpDuration.millis % 1000

        if ((!millis) && (milliSecs > 500)) {
            // If we don't want to display milliseconds, we'll round if necessary
            tmpDuration = tmpDuration.plus(1000 - milliSecs)   // This should round up to the nearest second
        }

        def builder = builderFor(tmpDuration.millis)

        if (millis) {
            builder.appendSeparator('.')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendMillis3Digit()
        }

        return builder.toFormatter().print(tmpDuration.toPeriod())
    }

    private def builderFor(long millis) {
        final long MILLIS_PER_MINUTE = 1000 * 60
        final long MILLIS_PER_TEN_MINUTES = MILLIS_PER_MINUTE * 10
        final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60

        if (millis < MILLIS_PER_MINUTE) {
            // '0:00' to '0:59'
            return new PeriodFormatterBuilder()
                    .appendLiteral('0:00:')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendSeconds()
        } else if (millis < MILLIS_PER_TEN_MINUTES) {
            // '1:00' to '9:59'
            return new PeriodFormatterBuilder()
                    .printZeroNever()
                    .minimumPrintedDigits(2)
                    .appendMinutes()
                    .appendSeparator(':')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendSeconds()
        } else if (millis < MILLIS_PER_HOUR) {
            // '10:00' to '59:59'
            return new PeriodFormatterBuilder()
                    .printZeroNever()
                    .minimumPrintedDigits(2)
                    .appendMinutes()
                    .appendSeparator(':')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendSeconds()
        } else {
            // '1:00:00' and above, e.g. '12:34:56', '123:45:54'
            return new PeriodFormatterBuilder()
                    .printZeroAlways()
                    .minimumPrintedDigits(1)
                    .appendHours()
                    .appendSuffix(':')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendMinutes()
                    .appendSuffix(':')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendSeconds()
        }
    }

    public def numberWithRealName = { attrs ->

        def number = attrs.number?.encodeAsHTML() ?: 'Unknown'

        if(attrs.user != null ) {
            return "${attrs.user.realName} (${number})"
        } else {
            return "${number}"
        }
    }
}