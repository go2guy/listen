package com.interact.listen.acd

import grails.plugin.springsecurity.annotation.Secured
import com.interact.listen.acd.AcdQueueStatus
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.PhoneNumber

@Secured(['ROLE_ACD_USER'])
class AcdController {
    static allowedMethods = [
        index: 'GET',
        status: 'GET',
        updateStatus: 'POST'
    ]

    def index = {
        redirect(action: 'status')
    }

    def updateStatus = {
        /* log.debug "AcdController.updateStatus: params[${params}]" */
        def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
        acd_user_status.acdQueueStatus = AcdQueueStatus.findByName(params.status)
        acd_user_status.contactNumber = PhoneNumber.findByNumber(params.contactNumber)

        try {
          if (acd_user_status.validate())
            if (!acd_user_status.save(failOnError: true, flush: true))
              log.debug "Could not update user acd status."
        }
        catch (Exception e) {
          log.debug "Caught excpetion saving acd user status [${e}]"
        }

        redirect(action: 'status')
    }

    def status = {
        def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
        def status = acd_user_status?.acdQueueStatus?.name
        def contactNumber = acd_user_status?.contactNumber?.number
        def optionNames = []
        def phoneNumbers = []

        AcdQueueStatus.findAll().each() { option ->
          optionNames.add(option.name)
        }

        PhoneNumber.findAllByOwner(authenticatedUser).each() { number ->
          phoneNumbers.add(number.number)
        }

        /* log.debug "Rendering view with parameters [status: ${status}, optionNames: ${optionNames}, phoneNumbers: ${phoneNumbers}, contactNumber: ${contactNumber}" */
        render(view: 'status', model: [status: status, optionNames: optionNames, phoneNumbers: phoneNumbers, contactNumber: contactNumber])
    }
}
