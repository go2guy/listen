package com.interact.listen

import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_SPOT_API'])
class CallRoutingController {
    
    static allowedMethods = [
        routeCall: 'GET'
    ]

    def callRoutingService

    def routeCall = {
        def ani = params.ani
        if(!ani || ani.trim() == '') {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [ani]')
            return
        }

        def dnis = params.dnis
        if(!dnis || dnis.trim() == '') {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [dnis]')
            return
        }

        def result = callRoutingService.routeCall(ani, dnis)

        if(!result) {
            response.sendError(HSR.SC_NOT_FOUND)
        }

        render(contentType: 'application/json') {
            application = result.application
            organization = {
                id = result.organization.id
            }
        }
    }
}

    
