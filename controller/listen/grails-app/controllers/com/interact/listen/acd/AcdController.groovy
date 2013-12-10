package com.interact.listen.acd

/* import grails.plugins.springsecurity.Secured */

import grails.plugin.springsecurity.annotation.Secured
import com.interact.listen.acd.Status

@Secured(['ROLE_ACD_USER'])
class AcdController {
    static allowedMethods = [
        index: 'GET',
        status: 'POST',
        updateStatus: 'POST'
    ]

    def index = {
        redirect(action: 'status')
    }

    def updateStatus = {
        log.debug("params.status [${params.status}]")
        def cu = authenticatedUser
        cu.status = Status.findByName(params.status)
        cu.save(flush: true)
        redirect(action: 'status')
    }

    def status = {
        def status = authenticatedUser?.status?.name
        def options = Status.findAll()
        def option_names = []

        options.each() { option ->
          option_names.add(option.name)
        }

        render(view: 'status', model: [status: status, option_names: option_names])
    }
}
