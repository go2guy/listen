package com.interact.listen

import com.interact.listen.pbx.Extension
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

        if(!shouldRoute(ani) || !shouldRoute(dnis)) {
            response.sendError(HSR.SC_FORBIDDEN)
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

    private boolean shouldRoute(def number) {
        def extension = Extension.findByNumber(number)
        if(extension && !extension.owner.enabled()) {
            return false
        }

        def direct = DirectMessageNumber.findByNumber(number)
        return !direct || direct.owner.enabled()
    }
}
