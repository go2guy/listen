package com.interact.listen.fax

import com.interact.listen.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import java.net.URI
import javax.servlet.http.HttpServletResponse

@Secured(['ROLE_SPOT_API'])
class FaxApiController {

    static allowedMethods = [
        create: 'POST',
        updateOutgoingFax: 'POST'
    ]

    def historyService
    def voicemailNotificationService
    
    def create = {
        def json = JSON.parse(request)
        response.status = HttpServletResponse.SC_CREATED

        log.debug "PAGES: ${json.pages}"

        def fax = new Fax()
        fax.ani = json.ani
        fax.file = new File(new URI(json.file))
        fax.owner = User.get(json.owner.id)
        fax.pages = json.pages

        if(!fax.owner.enabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        if(fax.validate() && fax.save()) {
            historyService.leftFax(fax)
            voicemailNotificationService.sendNewFaxEmail(fax)
            voicemailNotificationService.sendNewFaxSms(fax)
        } else {
            log.error(fax.errors)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }

        response.flushBuffer()
    }

    def updateOutgoingFax = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)
        fax.properties['attempts', 'pages', 'status'] = json

        if(fax.validate() && fax.save()) {
            response.status = HttpServletResponse.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, beanErrors(fax))
        }
    }

    private def beanErrors(def bean) {
        def result = new StringBuilder()
        g.eachError(bean: bean) {
            result << g.message(error: it)
            result << "\n"
        }
        return result.toString()
    }
}
