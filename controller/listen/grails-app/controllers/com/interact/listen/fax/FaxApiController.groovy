package com.interact.listen.fax

import com.interact.listen.User
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import java.net.URI
import javax.servlet.http.HttpServletResponse

@Secured(['ROLE_SPOT_API'])
class FaxApiController {

    static allowedMethods = [
        create: 'POST'
    ]

    def historyService
    
    def create = {
        def json = JSON.parse(request)
        response.status = HttpServletResponse.SC_CREATED

        def fax = new Fax()
        fax.ani = json.ani
        fax.file = new File(new URI(json.file))
        fax.owner = User.get(json.owner.id)

        if(fax.validate() && fax.save()) {
            historyService.leftFax(fax)
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        }

        response.flushBuffer()
    }

}