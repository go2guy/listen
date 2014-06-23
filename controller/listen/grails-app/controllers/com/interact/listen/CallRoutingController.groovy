package com.interact.listen

import com.interact.listen.pbx.Extension
import grails.plugin.springsecurity.annotation.Secured
//import grails.plugins.springsecurity.Secured
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
            log.debug "route call without result"
            response.sendError(HSR.SC_NOT_FOUND)
            render(contentType: 'application/json') {}
        } else {
            log.debug "route call with result [${result}]"
            
            render(contentType: 'application/json') {
                application = result?.application
                organization = {
                    id = result?.organization?.id
                }
            }
        }
    }

    // Leaving this method here even though it isn't used currently.  We previously were calling this method before 
    // calling the callRoutingService. Apps group has requested we not do this check here as they wouldn't have a 
    // graceful way of handling a 403 at this point in processing.  Leaving the method incase we want to re-add later.
    // Changes made for Target Process bug 26927
/*    private boolean shouldRoute(def number) {
        def extension = Extension.findByNumber(number)
        if(extension && !extension.owner.enabled()) {
            return false
        }

        def direct = DirectMessageNumber.findByNumber(number)
        return !direct || direct.owner.enabled()
    }*/
}
