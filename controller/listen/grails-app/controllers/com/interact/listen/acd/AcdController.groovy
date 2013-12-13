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
        acd_user_status.acdQueueStatus = AcdQueueStatus.findByName(params.status)
        acd_user_status.save(flush: true)
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
