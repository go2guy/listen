package com.interact.listen

import com.interact.listen.voicemail.Voicemail
import grails.converters.*
import grails.plugins.springsecurity.Secured
import org.joda.time.format.PeriodFormatterBuilder

@Secured(['ROLE_VOICEMAIL_USER', 'ROLE_FAX_USER'])
class MessagesController {
    static allowedMethods = [
        index: 'GET',
        inbox: 'GET',
        delete: 'POST',
        newCount: 'GET',
        pollingList: 'GET',
        toggleStatus: 'POST',
        setStatus: 'GET'
    ]

    def inboxMessageService

    def index = {
        redirect(action: 'inbox')
    }

    def inbox = {
        def user = authenticatedUser

        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        def list = InboxMessage.findAllByOwner(user, params)
        def count = InboxMessage.countByOwner(user)

        render(view: 'inbox', model: [messageList: list, messageTotal: count])
    }

    def delete = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        // TODO log errors

        def message = InboxMessage.get(params.id)
        if(!message) {
            flash.errorMessage = 'Message not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = authenticatedUser
        if(message.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        inboxMessageService.delete(message)

        flash.successMessage = 'Message deleted'
        redirect(action: 'inbox', params: preserve)
    }

    // ajax
    def newCount = {
        render(contentType: 'application/json') {
            count = inboxMessageService.newMessageCount()
        }
    }

    def toggleStatus = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        // TODO log errors

        def message = InboxMessage.get(params.id)
        if(!message) {
            flash.errorMessage = 'Message not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = authenticatedUser
        if(message.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        inboxMessageService.toggleStatus(message)

        flash.successMessage = 'Message status updated'
        redirect(action: 'inbox', params: preserve)
    }
    
    // ajax
    def pollingList = {
        def user = authenticatedUser

        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        def list = InboxMessage.findAllByOwner(user, params)
        def visibleIds = params.visibleIds.trim()
        if(visibleIds.length() > 0) {
            visibleIds = visibleIds.split(",")
        }
        
        def changes = [:]
        def returnList = []
        def currentIds = []
        def addToScreen = []
        def removeFromScreen = []
        def updatedVisibleIds = []
        def formatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendMinutes()
            .appendSuffix('m, ')
            .printZeroAlways()
            .minimumPrintedDigits(1)
            .appendSeconds()
            .appendSuffix('s')
            .toFormatter()

        list.each { 
            currentIds << String.valueOf(it.id)
            def item = [
                id: it.id,
                from: it.from().encodeAsHTML(),
                dateCreated: listen.prettytime(date: it.dateCreated),
                dateTitle: joda.format(value: it.dateCreated, style: 'LL'),
                isNew: it.isNew
            ]

            if(it.instanceOf(Voicemail)) {
                item.put('type', 'voicemail')
                item.put('audio', [
                    duration: formatter.print(it.audio.duration.toPeriod())
                ])
                item.put('transcription', it.audio.transcription.encodeAsHTML())
            } else {
                item.put('type', 'fax')
                item.put('size', listen.megabytes(file: it.file, unavailable: 'Size Unknown'))
            }

            returnList << item
        }
        visibleIds.each { updatedVisibleIds.add(it) }

        def i = 0
        visibleIds.each {
            if(!currentIds.contains(visibleIds[i])) {
                removeFromScreen.add(i)
                updatedVisibleIds.each {
                    if(it == i) {
                        updatedVisibleIds.remove(it)
                    }
                }
            }
            i++
        }

        i = 0
        currentIds.each {
            if(!updatedVisibleIds.contains(currentIds[i])) {
                addToScreen.add(i)
            }
            i++
        }

        changes["add"] = addToScreen
        changes["remove"] = removeFromScreen
        changes["list"] = returnList
        changes["currentIds"] = currentIds

        render changes as JSON
    }
    
    def setStatus = {
        def message = InboxMessage.get(params.id)
        if(!message) {
            response.sendError(HSR.SC_NOT_FOUND, 'Message not found')
            response.flushBuffer()
            return
        }

        def user = authenticatedUser
        if(message.owner != user) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Current user not owner of message')
            response.flushBuffer()
            return
        }

        inboxMessageService.setStatus(message, params.newStatus == 'new')
        response.flushBuffer()
    }

    // FIXME used by the polling list, which needs to account for faxes
    private String getMp3Uri(String uri) {
        if(uri.endsWith(".wav"))
        {
            uri = uri.replace(".wav", ".mp3");
        }
        
        if(!uri.endsWith(".mp3"))
        {
            uri.concat(".mp3");
        }

        return uri.encodeAsHTML();
    }
}
