package com.interact.listen.acd

import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.PhoneNumber
import com.interact.listen.history.*
import org.joda.time.DateTime

import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ACD_USER'])
class AcdController {
    
    def historyService
    
    static allowedMethods = [
        index: 'GET',
        status: 'GET',
        toggleStatus: 'POST',
        updateNumber: 'POST'
    ]

    def index = {
      redirect(action: 'status')
    }

    def callQueue = {
        def calls = AcdCall.findAll()
        render(view: 'callQueue', model: [calls: calls])
    }
    
    def pollQueue = {
        def json = [:]
        json.calls = AcdCall.findAll()
        
        render(contentType: 'application/json') {
          json
        }
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
        acdUserStatus.statusModified = DateTime.now();
        
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
        def statusDisabled = acdUserStatus?.acdQueueStatus?.isDisabled();
        def contactNumber = acdUserStatus?.contactNumber?.number
        def optionNames = AcdQueueStatus.values()
        
        log.debug "User status contact number [${contactNumber}]"
        
        def phoneNumbers = []
        PhoneNumber.findAllByOwner(authenticatedUser).each() { number ->
            phoneNumbers.add(number.number)
            log.debug "Adding phone number [${number.number}]"
        }
        
        def model = [
            status: status,
            statusDisabled: statusDisabled,
            optionNames: optionNames,
            phoneNumbers: phoneNumbers,
            contactNumber: contactNumber
          ]
        
        log.debug "Rendering view [status] with model [${model}]"
        render(view: 'status', model: model)
    }

}
