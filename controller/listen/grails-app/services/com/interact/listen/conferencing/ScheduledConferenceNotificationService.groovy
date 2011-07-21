package com.interact.listen.conferencing

import com.interact.listen.pbx.NumberRoute
import com.interact.listen.stats.Stat

import org.apache.commons.validator.EmailValidator
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat

class ScheduledConferenceNotificationService {
    static transactional = false

    def backgroundService
    def grailsApplication
    def statWriterService

    void sendEmails(ScheduledConference scheduledConference) {
        def adminBody = vcalMarkup(scheduledConference, PinType.ADMIN)
        def activeBody = vcalMarkup(scheduledConference, PinType.ACTIVE)
        def passiveBody = vcalMarkup(scheduledConference, PinType.PASSIVE)
        def vcalHeaders = vcalHeaders()

        backgroundService.execute("Scheduled conference email for ${scheduledConference}", {
            def addresses = getValidEmails(scheduledConference)
            if(addresses.active.size() > 0) {
                log.debug "Sending conference invitation (active) to ${addresses.active}"
                sendMail {
                    headers vcalHeaders
                    to addresses.active.toArray()
                    subject "Listen Conference Invitation: ${scheduledConference.emailSubject}"
                    body activeBody
                }
            }

            if(addresses.passive.size() > 0) {
                log.debug "Sending conference invitation (passive) to ${addresses.passive}"
                sendMail {
                    headers vcalHeaders
                    to addresses.passive.toArray()
                    subject "Listen Conference Invitation: ${scheduledConference.emailSubject}"
                    body passiveBody
                }
            }

            log.debug "Sending conference invitation (admin) to ${scheduledConference.scheduledBy.emailAddress}"
            sendMail {
                headers vcalHeaders
                to scheduledConference.scheduledBy.emailAddress
                subject "Listen Conference Invitation: ${scheduledConference.emailSubject}"
                body adminBody
            }
        })
        statWriterService.send(Stat.CONFERENCE_INVITE_EMAIL)
    }

    void sendCancellation(ScheduledConference scheduledConference) {
        def adminBody = vcalCancelMarkup(scheduledConference, PinType.ADMIN)
        def activeBody = vcalCancelMarkup(scheduledConference, PinType.ACTIVE)
        def passiveBody = vcalCancelMarkup(scheduledConference, PinType.PASSIVE)
        def vcalHeaders = vcalCancelHeaders()

        backgroundService.execute("Cancel conference invitation email for ${scheduledConference}", {
            def addresses = getValidEmails(scheduledConference)
            if(addresses.active.size() > 0) {
                log.debug "Sending conference cancel invitation (active) to ${addresses.active}"
                sendMail {
                    headers vcalHeaders
                    to addresses.active.toArray()
                    subject "Canceled: ${scheduledConference.emailSubject}"
                    body activeBody
                }
            }

            if(addresses.passive.size() > 0) {
                log.debug "Sending conference cancel invitation (passive) to ${addresses.passive}"
                sendMail {
                    headers vcalHeaders
                    to addresses.passive.toArray()
                    subject "Canceled: ${scheduledConference.emailSubject}"
                    body passiveBody
                }
            }

            log.debug "Sending conference cancel invitation (admin) to ${scheduledConference.scheduledBy.emailAddress}"
            sendMail {
                headers vcalHeaders
                to scheduledConference.scheduledBy.emailAddress
                subject "Canceled: ${scheduledConference.emailSubject}"
                body adminBody
            }
        })
        statWriterService.send(Stat.CONFERENCE_CANCEL_EMAIL)
    }

    private def getEmailBody(ScheduledConference scheduledConference, PinType pinType) {
        def organization = scheduledConference.forConference.owner.organization
        // TODO hard-coded destination application
        def phoneNumbers = NumberRoute.withCriteria {
            isNotNull('label')
            eq('organization', organization)
            eq('destination', 'Conferencing')
        }
        def phoneNumberCount = phoneNumbers.size()
        def phoneNumberHtml = ''
        if(phoneNumberCount == 0) {
            phoneNumberHtml = 'Contact the conference administrator for the conference phone number.'
        } else {
            phoneNumberHtml = 'Join the conference by dialing:\n'
            phoneNumbers.each { number ->
                phoneNumberHtml += "- ${number.label}: ${number.pattern}\n"
            }
        }

        def pinHtml = ''
        switch(pinType) {
            case PinType.ACTIVE:
            case PinType.PASSIVE:
                pinHtml += "${pinType.displayName()} PIN: ${getPin(scheduledConference, pinType)}\n"
                break
            case PinType.ADMIN:
                Pin.findAllByConference(scheduledConference.forConference).each { pin ->
                    pinHtml += "${pin.pinType.displayName()} PIN: ${pin.number}\n"
                }
                break
        }

        def body = """You have been invited to a conference by ${scheduledConference.scheduledBy.friendlyName()}.
Subject: ${scheduledConference.emailSubject}
Date/Time: ${formattedDateTime(scheduledConference)}
Memo: ${scheduledConference.emailBody}

${pinHtml}

${phoneNumberHtml}
"""

        //in case the user create memo has new lines
        body = body.replaceAll('\\r', '')
        return body.replaceAll('\\n', '\\\\n')
    }

    private def getHtmlBody(ScheduledConference scheduledConference, PinType pinType) {
        def organization = scheduledConference.forConference.owner.organization
        // TODO hard-coded destination application
        def phoneNumbers = NumberRoute.withCriteria {
            isNotNull('label')
            eq('organization', organization)
            eq('destination', 'Conferencing')
        }
        def phoneNumberCount = phoneNumbers.size()
        def phoneNumberHtml = ''
        if(phoneNumberCount == 0) {
            phoneNumberHtml = 'Contact the conference administrator for the conference phone number.'
        } else {
            phoneNumberHtml = 'Join the conference by dialing:<br/>'
            phoneNumbers.each { number ->
                    phoneNumberHtml += "- ${number.label}: ${number.pattern}<br/>"
            }
        }

        def pinHtml = ''
        switch(pinType) {
            case PinType.ACTIVE:
            case PinType.PASSIVE:
                pinHtml += "<b>${pinType.displayName()} PIN: ${getPin(scheduledConference, pinType)}</b><br/>"
                break
            case PinType.ADMIN:
                Pin.findAllByConference(scheduledConference.forConference).each { pin ->
                    pinHtml += "<b>${pin.pinType.displayName()} PIN: ${pin.number}</b><br/>"
                }
                break
        }

        def body = """\
<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\
<html><body>\
You have been invited to a conference by ${scheduledConference.scheduledBy.friendlyName().encodeAsHTML()}.<br/><b\
r/>\
\
Subject: ${scheduledConference.emailSubject.encodeAsHTML()}<br/>\
Date/Time: ${formattedDateTime(scheduledConference)}<br/>\
Memo: ${scheduledConference.emailBody.encodeAsHTML()}<br/><br/>\
\
${pinHtml}<br/>\
\
${phoneNumberHtml}\
</body></html>\
"""
        //in case the user create memo has new lines
        body = body.replaceAll('\\r', '<br/>')
        return body.replaceAll('\\n', '<br/>')
    }

    private def getPin(ScheduledConference sc, PinType pinType) {
        Pin.findByConferenceAndPinType(sc.forConference, pinType).number
    }

    private def formattedDateTime(ScheduledConference sc) {
        def dateformat = DateTimeFormat.forPattern('MMM d, yyyy')
        def timeformat = DateTimeFormat.forPattern('h:mm a')
        return "${dateformat.print(sc.date)} from ${timeformat.print(sc.starts)} to ${timeformat.print(sc.ends)}"
    }

    private def getValidEmails(ScheduledConference scheduledConference) {
        def validator = EmailValidator.instance
        def emails = [:]
        emails.active = scheduledConference.activeCallers(false).findAll {
            validator.isValid(it)
        }
        emails.passive = scheduledConference.passiveCallers(false).findAll {
            validator.isValid(it)
        }
        return emails
    }

    private def vcalMarkup(ScheduledConference sc, PinType pinType) {
        def b = new StringBuilder()

        def emails = getValidEmails(sc)
        def iso = ISODateTimeFormat.basicDateTimeNoMillis()

        b << 'BEGIN:VCALENDAR\n'
        b << 'PRODID:-//Interact Incorporated//Listen//EN\n'
        b << 'VERSION:2.0\n'
        b << 'METHOD:REQUEST\n'
        b << 'X-MS-OLK-FORCEINSPECTOROPEN:TRUE\n'
        b << 'BEGIN:VEVENT\n'
        emails.active.each {
            b << "ATTENDEE;CN=${it};RSVP=TRUE:mailto:${it}\n"
        }
        emails.passive.each {
            b << "ATTENDEE;CN=${it};RSVP=TRUE:mailto:${it}\n"
        }
        b << 'CLASS:PUBLIC\n'
        b << "CREATED:${iso.withZone(DateTimeZone.UTC).print(sc.dateCreated)}\n"
        b << "DESCRIPTION:${getEmailBody(sc, pinType)}\n"
        b << "DTEND:${iso.withZone(DateTimeZone.UTC).print(sc.endsAt().toDateTime())}\n"
        b << "DTSTAMP:${iso.withZone(DateTimeZone.UTC).print(sc.dateCreated)}\n"
        b << "DTSTART:${iso.withZone(DateTimeZone.UTC).print(sc.startsAt().toDateTime())}\n"
        b << "LAST-MODIFIED:${iso.withZone(DateTimeZone.UTC).print(sc.dateCreated)}\n"
        b << 'LOCATION:Listen\n'
        b << "ORGANIZER;CN=\"${sc.scheduledBy.realName}\":mailto:${sc.scheduledBy.emailAddress}\n"
        b << 'PRIORITY:5\n'
        b << 'SEQUENCE:0\n'
        b << "SUMMARY;LANGUAGE=en-us:${sc.emailSubject}\n"
        b << 'TRANSP:OPAQUE\n'
        b << "UID:${sc.uid}\n"
        b << "X-ALT-DESC;FMTTYPE=text/html:${getHtmlBody(sc, pinType)}\n"
        b << 'X-MICROSOFT-CDO-BUSYSTATUS:TENTATIVE\n'
        b << 'X-MICROSOFT-CDO-IMPORTANCE:1\n'
        b << 'X-MICROSOFT-CDO-INTENDEDSTATUS:BUSY\n'
        b << 'X-MICROSOFT-DISALLOW-COUNTER:FALSE\n'
        b << 'X-MS-OLK-ALLOWEXTERNCHECK:TRUE\n'
        b << 'X-MS-OLK-AUTOSTARTCHECK:FALSE\n'
        b << 'X-MS-OLK-CONFTYPE:0\n'
        b << "X-MS-OLK-SENDER;CN=\"${sc.scheduledBy.realName}\":mailto:${sc.scheduledBy.emailAddress}\n"
        b << 'BEGIN:VALARM\n'
        b << 'TRIGGER:-PT15M\n'
        b << 'ACTION:DISPLAY\n'
        b << 'DESCRIPTION:Reminder\n'
        b << 'END:VALARM\n'
        b << 'END:VEVENT\n'
        b << 'END:VCALENDAR\n'

        log.debug "TONY vcal = ${b.toString()}"
        return b.toString()
    }

    private def vcalCancelMarkup(ScheduledConference sc, PinType pinType) {
        def b = new StringBuilder()

        def emails = getValidEmails(sc)
        def iso = ISODateTimeFormat.basicDateTimeNoMillis()

        b << 'BEGIN:VCALENDAR\n'
        b << 'PRODID:-//Interact Incorporated//Listen//EN\n'
        b << 'VERSION:2.0\n'
        b << 'METHOD:CANCEL\n'
        b << 'X-MS-OLK-FORCEINSPECTOROPEN:TRUE\n'
        b << 'BEGIN:VEVENT\n'
        emails.active.each {
            b << "ATTENDEE;CN=${it};RSVP=TRUE:mailto:${it}\n"
        }
        emails.passive.each {
            b << "ATTENDEE;CN=${it};RSVP=TRUE:mailto:${it}\n"
        }
        b << 'CLASS:PUBLIC\n'
        b << "CREATED:${iso.withZone(DateTimeZone.UTC).print(sc.dateCreated)}\n"
        b << "DESCRIPTION:${getEmailBody(sc, pinType)}\n"
        b << "DTEND:${iso.withZone(DateTimeZone.UTC).print(sc.endsAt().toDateTime())}\n"
        b << "DTSTAMP:${iso.withZone(DateTimeZone.UTC).print(sc.dateCreated)}\n"
        b << "DTSTART:${iso.withZone(DateTimeZone.UTC).print(sc.startsAt().toDateTime())}\n"
        b << "LAST-MODIFIED:${iso.withZone(DateTimeZone.UTC).print(new DateTime())}\n"
        b << 'LOCATION:Listen\n'
        b << "ORGANIZER;CN=\"${sc.scheduledBy.realName}\":mailto:${sc.scheduledBy.emailAddress}\n"
        b << 'PRIORITY:1\n'
        b << 'SEQUENCE:1\n'
        b << "SUMMARY;LANGUAGE=en-us:Canceled: ${sc.emailSubject}\n"
        b << 'TRANSP:TRANSPARENT\n'
        b << "UID:${sc.uid}\n"
        b << "X-ALT-DESC;FMTTYPE=text/html:${getHtmlBody(sc, pinType)}\n"
        b << 'X-MICROSOFT-CDO-BUSYSTATUS:FREE\n'
        b << 'X-MICROSOFT-CDO-IMPORTANCE:2\n'
        b << 'X-MICROSOFT-DISALLOW-COUNTER:FALSE\n'
        b << 'X-MS-OLK-ALLOWEXTERNCHECK:TRUE\n'
        b << 'X-MS-APPTSEQTIME:${iso.withZone(DateTimeZone.UTC).print(new DateTime())}\n'
        b << 'X-MS-OLK-AUTOSTARTCHECK:FALSE\n'
        b << 'X-MS-OLK-CONFTYPE:0\n'
        b << "X-MS-OLK-SENDER;CN=\"${sc.scheduledBy.realName}\":mailto:${sc.scheduledBy.emailAddress}\n"
        b << 'END:VEVENT\n'
        b << 'END:VCALENDAR\n'
        return b.toString()
    }

    private def vcalHeaders() {
        return [
            'Content-Type': 'text/calendar; method=REQUEST; charset="UTF-8"',
            'Content-Transfer-Encoding': '7bit'
        ]
    }
    
    private def vcalCancelHeaders() {
        return [
            'Content-Type': 'text/calendar; method=CANCEL; charset="UTF-8"',
            'Content-Transfer-Encoding': '7bit'
        ]
    }
}
