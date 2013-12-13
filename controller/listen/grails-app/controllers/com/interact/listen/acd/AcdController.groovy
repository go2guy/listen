package com.interact.listen.acd

import grails.plugin.springsecurity.annotation.Secured
import com.interact.listen.acd.AcdQueueStatus
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.pbx.Extension

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
        def cu = authenticatedUser
        cu.acdUserStatus.acdQueueStatus = AcdQueueStatus.findByName(params.status)
        cu.save(flush: true)
        redirect(action: 'status')
    }

    def status = {
        def status = authenticatedUser?.acdUserStatus?.acdQueueStatus?.name
        def options = AcdQueueStatus.findAll()
        def option_names = []

        options.each() { option ->
          option_names.add(option.name)
        }

        /* def phoneNumbers = [] */
        /* PhoneNumber.findAll().each() { number -> */
          /* phoneNumbers.add(number.number) */
        /* } */

        /* render(view: 'status', model: [status: status, option_names: option_names, phoneNumbers: phoneNumbers]) */
        render(view: 'status', model: [status: status, option_names: option_names])
    }
}
