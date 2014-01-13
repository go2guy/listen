package com.interact.listen.acd

import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.acd.Skill
import com.interact.listen.PhoneNumber
import com.interact.listen.history.*
import org.joda.time.DateTime
import com.interact.listen.util.FileTypeDetector
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ACD_USER'])
class AcdController {
    static allowedMethods = [
      index: 'GET',
      status: 'GET',
      toggleStatus: 'POST',
      updateNumber: 'POST',
      callQueue: 'GET',
      pollQueue: 'GET'
    ]
    
    def promptFileService
    def historyService
    
    // TODO fix hard-coded path
    static final File storageLocation = new File('/interact/listen/artifacts/acd')
    
    def index = {
      redirect(action: 'status')
    }

    def callQueue = {
      def calls = AcdCall.findAll("from AcdCall as calls order by calls.enqueueTime")
      render(view: 'callQueue', model: [calls: calls])
    }

    def pollQueue = {
      def json = [:]
      json.calls = AcdCall.findAll("from AcdCall as calls order by calls.${params.orderBy}")
      /* For w/e reason, grails only populates the call.skill property with the id for
         the skill, so we need to create a skills field within the json referenced by
         skill id to look up the call's requested skill */
      json.skills = Skill.findAll()

      log.debug "Rendering call queue as json [${json.toString()}]"
      render(contentType: 'application/json') {
        json
      }
    }

    def status = {
      def acdUserStatus = AcdUserStatus.findByOwner(authenticatedUser)
      if (!acdUserStatus) {
          log.debug "user does not currently have an acd user status [${acdUserStatus}]"
          acdUserStatus = new AcdUserStatus()
          acdUserStatus.owner = authenticatedUser
          acdUserStatus.acdQueueStatus = AcdQueueStatus.Unavailable
          /* Create user acd status entry, as they should have had one already */
          if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true)) {
              log.error "Created acd user status for this user [${acdUserStatus.owner}]"
          } else {
              log.error "Could not create Acd Status Entry for new user."
          }
      }


      def status = acdUserStatus?.acdQueueStatus?.toString()
      def statusDisabled = acdUserStatus?.acdQueueStatus?.isDisabled()
      def contactNumber = acdUserStatus?.contactNumber?.number
      def phoneNumbers = []

      log.debug "User status contact number [${contactNumber}]"

      PhoneNumber.findAllByOwner(authenticatedUser).each() { number ->
        phoneNumbers.add(number.number)
      }

      def model = [
        status: status,
        statusDisabled: statusDisabled,
        phoneNumbers: phoneNumbers,
        contactNumber: contactNumber
      ]

      log.debug "Rendering view [status] with model [${model}]"
      render(view: 'status', model: model)
    }

    def toggleStatus = {
        log.debug "AcdController.toggleStatus: params[${params}]"
        def acdUserStatus = AcdUserStatus.findByOwner(authenticatedUser)
        if (!acdUserStatus) {
            log.error "Failed to find acd user status, maybe not serious"
            flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
            redirect(action: 'status')
            return
        }

        log.debug "Before Toggle user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"

        acdUserStatus.toggleStatus()
        acdUserStatus.statusModified = DateTime.now()

        log.debug "Updating user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"

        if (acdUserStatus.acdQueueStatus && acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true)) {
            log.debug "Successfully changed user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"
            historyService.toggleACDStatus(acdUserStatus)
            flash.successMessage = message(code: 'page.acd.status.statusChange.successful.message', args: [acdUserStatus.acdQueueStatus])
        } else {
            log.error "Could not update user acd status."
            flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
        }

        redirect(action: 'status')
        return
    }

    def updateNumber = {
        log.debug "AcdController.updateNumber: params[${params}]"
        def acdUserStatus = AcdUserStatus.findByOwner(authenticatedUser)
        if (!acdUserStatus) {
            log.error "Failed to find acd user status, maybe not serious"
            flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
            redirect(action: 'status')
            return
        }

        acdUserStatus.contactNumber = PhoneNumber.findByOwnerAndNumber(authenticatedUser, params?.contactNumber)

        if (acdUserStatus.contactNumber && acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true)) {
            log.debug "Successfully changed user [${acdUserStatus.owner.username}] to contact number [${acdUserStatus.contactNumber.number}]"
            historyService.updatedACDContactNumber(acdUserStatus)
            flash.successMessage = message(code: 'page.acd.status.statusChange.successful.message', args: [acdUserStatus.contactNumber.number])
        } else {
            log.error "Failed to find phone number [${params?.contactNumber}]"
            flash.errorMessage = message(code: 'page.acd.status.statusNumber.failure.message', args: [params?.contactNumber])
        }

        redirect(action: 'status')
    }
    
    def uploadPrompt = {
        def file = request.getFile('uploadFile')
        if(!file) {
            render('Please select a file to upload')
            return
        }

        def detector = new FileTypeDetector()
        def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
        if(detectedType != 'audio/x-wav') {
            render('File must be a wav file')
            return
        }

        def user = authenticatedUser
        promptFileService.save(storageLocation, file, user.organization.id)

        render('Success')
    }

}
