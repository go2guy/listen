package com.interact.listen

import com.interact.listen.acd.*
// import com.interact.listen.acd.UserSkill
import com.interact.listen.voicemail.Voicemail
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import org.joda.time.format.PeriodFormatterBuilder

import javax.servlet.http.HttpServletRequest

@Secured(['ROLE_VOICEMAIL_USER', 'ROLE_FAX_USER'])
class MessagesController {
    static allowedMethods = [
        index: 'GET',
        inbox: 'GET',
        acdInbox: ['GET', 'POST'],
        delete: 'POST',
        newCount: 'GET',
        newAcdCount: 'GET',
        pollingList: 'GET',
        acdPollingList: 'GET',
        toggleStatus: 'POST',
        setStatus: 'GET'
    ]

    def inboxMessageService
    def springSecurityService

    def index = {
        redirect(action: 'inbox')
    }

    def inbox =
    {
        if(!springSecurityService.currentUser)
        {
            //Redirect to login
            redirect(controller: 'login', action: 'auth');
        }
        def user = springSecurityService.currentUser

        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        def list = InboxMessage.findAllByOwner(user, params)
        def count = InboxMessage.countByOwner(user)
        
        log.debug "Found [${count}] messages in mailbox of [${user.username}]"

        list.each { message ->
            
            if(message.instanceOf(Voicemail)) {
                log.debug "Voice Message [${message.id}] [${message?.audio.file}]"
            } else if(message.instanceOf(Fax)) {
                log.debug "Fax Message [${message.id}] [${message?.fax.file}]"
            } else {
                log.debug "Unknown Message Type [${message.id}] [${message}]"
            }
        }
        
        render(view: 'inbox', model: [messageList: list, messageTotal: count])
    }

    def acdInbox = {
      def user = springSecurityService.currentUser
      def voicemailUser
      def skillList = []
      def count = ""
      def messageList = []
      def messageTotal = 0

      // reconcile sorting parameters
      params.max = Math.min(params.max ? params.int('max') : 25, 100)
      params.sort = params.sort ? params.sort : 'dateCreated'
      params.order = params.order ? params.order : 'desc'
      params.currentSkill = params.currentSkill ?: 'All'

      def currentSkill = params.currentSkill

      // compile an associated skill list to populate the select
      def userSkills = UserSkill.findAllByUser(user)
      def newMessageTotal = 0
      userSkills?.each() { userSkill ->
        count = inboxMessageService.newAcdMessageCount(userSkill?.skill?.skillname)
        newMessageTotal += count.substring(count.indexOf("(")+1,count.indexOf(")")).toInteger()
        skillList.push( (userSkill?.skill?.skillname + " " + count ))
      }
      skillList.push( ("All (" + newMessageTotal + ")") )

      def userList = []
      userSkills?.each() { userSkill ->
        voicemailUser = AcdService.getVoicemailUserBySkillname(userSkill?.skill?.skillname)
        userList.add(voicemailUser)
        messageTotal += InboxMessage.countByOwner(voicemailUser)
      }

      // use the current skillname to look up the current voicemail user
      if ( currentSkill != 'All' ) {
        voicemailUser = AcdService.getVoicemailUserBySkillname(currentSkill)

        messageList = InboxMessage.findAllByOwner(voicemailUser,params)
        messageTotal = InboxMessage.countByOwner(voicemailUser)

        log.debug "Found [${count}] messages in mailbox of [${voicemailUser?.username}]"
      }
      else { // All
        // If we didn't find any associated voicemail users there won't be any messages
        if ( !userList.isEmpty() ) {
          messageList = InboxMessage.createCriteria().list {
            order(params.sort,params.order)
            maxResults(params.max)
            'in'("owner",userList)
          }
        }
      }

      messageList.each() { message ->
        if(message.instanceOf(Voicemail)) {
            log.debug "Voice Message [${message.id}] [${message?.audio.file}]"
        } else if(message.instanceOf(Fax)) {
            log.debug "Fax Message [${message.id}] [${message?.fax.file}]"
        } else {
            log.debug "Unknown Message Type [${message.id}] [${message}]"
        }
      }

      def model = [
        messageList: messageList,
        messageTotal: messageTotal,
        skillList: skillList,
        currentSkill: currentSkill,
        sort: params.sort,
        order: params.order,
        max: params.max
      ]

      log.debug "Rendering view [acdInbox] with model [${model}]"
      render(view: 'acdInbox', model: model)
    }

    def delete = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max
        preserve.currentSkill = params.currentSkill ?: null

        // TODO log errors

        def message = InboxMessage.get(params.id)
        if(!message) {
            flash.errorMessage = 'Message not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = springSecurityService.currentUser
        // are they the voicemail owner?
        def authorized = (user == message.owner)
        def voicemailOwner = authorized

        // Apparently they don't own the voicemail...
        if (!voicemailOwner) {
          // does the voicemail belong to a user designated as an acd skill voicemail user?
          if ( message.owner.acdUserStatus.acdQueueStatus == AcdQueueStatus.VoicemailBox ) {
            // is the current user associated with this voicemail user's skill?
            def voicemailSkill = UserSkill.findByUser(message.owner).skill
            UserSkill.findAllByUser(user)?.each() { userSkill ->
              if ( userSkill.skill == voicemailSkill ) {
                authorized = true
              }
            }
          }
        }

        // if they're still not authorized, redirect to the login screen
        if ( !authorized ) {
          redirect(controller: 'login', action: 'denied')
          return
        }

        inboxMessageService.delete(message)

        flash.successMessage = 'Message deleted'
        if ( voicemailOwner ) {
          redirect(action: 'inbox', params: preserve)
        }
        else {
          redirect(action: 'acdInbox', params: preserve)
        }
    }

    // ajax
    def newCount = {
        HttpServletRequest theRequest = request;
        boolean isAjax = theRequest.xhr;

        if(!isAjax)
        {
            redirect(action: 'index');
        }

        render(contentType: 'application/json') {
            count = inboxMessageService.newMessageCount()
        }
    }

    // ajax
    def newAcdCount = {

        HttpServletRequest theRequest = request;
        boolean isAjax = theRequest.xhr;

        if(!isAjax)
        {
            redirect(action: 'index');
        }

      render(contentType: 'application/json') {
        count = inboxMessageService.newAcdMessageCount(params.currentSkill)
      }
    }

    def toggleStatus = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max
        preserve.currentSkill = params.currentSkill ?: null
        def referer = request.getHeader('referer')

        // TODO log errors

        def message = InboxMessage.get(params.id)
        if(!message) {
            flash.errorMessage = 'Message not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = springSecurityService.currentUser
        if ( message.owner != user && ! referer.contains('acdInbox') ) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        inboxMessageService.toggleStatus(message)

        flash.successMessage = 'Message status updated'

        if ( referer.contains("acdInbox") ) {
          redirect(action: 'acdInbox', params: preserve)
        }
        else {
          redirect(action: 'inbox', params: preserve)
        }
    }
    
    // ajax
    def pollingList = {
        def user = springSecurityService.currentUser

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
                item.put('pages', it.pages)
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

    // ajax
    def acdPollingList = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        params.currentSkill = params.currentSkill ? params.currentSkill : 'All'

        def user = springSecurityService.currentUser
        def currentSkill = params.currentSkill
        def voicemailUser
        def list = []

        // find message count for all skills associated with current user
        def messageCounts = [:]

        def userSkills = UserSkill.findAllByUser(user)

        userSkills.each() { userSkill ->
          messageCounts[userSkill.skill.skillname] = inboxMessageService.newAcdMessageCount(userSkill.skill.skillname)
        }

        messageCounts['All'] = inboxMessageService.newAcdMessageCount('All')

        // use the current skillname to look up the current voicemail user
        // and get their messages
        if ( currentSkill != 'All' ) {
          voicemailUser = AcdService.getVoicemailUserBySkillname(currentSkill)

          list = InboxMessage.findAllByOwner(voicemailUser,params)
        }
        else { // All
          // get Voicemail messages for all users
          def userList = []

          UserSkill.findAllByUser(user).each() { userSkill ->
            userList.add(AcdService.getVoicemailUserBySkillname(userSkill?.skill?.skillname))
          }

          list = InboxMessage.createCriteria().list {
            order(params.sort,params.order)
            maxResults(params.max)
            'in'("owner",userList)
          }
        }

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
                item.put('pages', it.pages)
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
        changes["messageCounts"] = messageCounts

        render changes as JSON
    }
    
    def setStatus = {
        def message = InboxMessage.get(params.id)
        if(!message) {
            response.sendError(HSR.SC_NOT_FOUND, 'Message not found')
            response.flushBuffer()
            return
        }

        def user = springSecurityService.currentUser
        if(message.owner != user) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Current user not owner of message')
            response.flushBuffer()
            return
        }

        inboxMessageService.setStatus(message, params.newStatus == 'new')
        response.flushBuffer()
    }
}
