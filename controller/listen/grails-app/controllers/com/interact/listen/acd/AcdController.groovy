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
        def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
        log.debug("params.status [${params.status}]")
        log.debug("params.contactNumber [${params.contactNumber}]")
        acd_user_status.acdQueueStatus = AcdQueueStatus.findByName(params.status)
        log.debug("acd_user_status.acdQueueStatus.name [${acd_user_status.acdQueueStatus.name}]")
        acd_user_status.contactNumber = PhoneNumber.findByNumber(params.contactNumber)
        log.debug("acd_user_status.contactNumber [${acd_user_status.contactNumber}]")

        try {
          if (acd_user_status.validate())
            acd_user_status.save(flush: true)
        }
        catch (Exception e) {
          log.debug "Caught excpetion saving acd user status [${e}]"
        }

        redirect(action: 'status')
    }

    def status = {
        def acd_user_status = AcdUserStatus.findByOwner(authenticatedUser)
        def status = acd_user_status?.acdQueueStatus?.name
        def contactNumber = acd_user_status?.contactNumber
        def optionNames = []
        def phoneNumbers = []

        AcdQueueStatus.findAll().each() { option ->
          optionNames.add(option.name)
        }

        PhoneNumber.findAllByOwner(authenticatedUser).each() { number ->
          phoneNumbers.add(number.number)
        }

        render(view: 'status', model: [status: status, optionNames: optionNames, phoneNumbers: phoneNumbers, contactNumber: contactNumber])
    }
}
