package com.interact.listen.pbx

import com.interact.listen.*
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_SPOT_API'])
class ExtensionController {

    static allowedMethods = [
        getExtensionsByOrganization: 'GET'
    ]

    def hrefParserService

    def getExtensionsByOrganization = {
        if(!params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [organization]')
            return
        }

        def organization = Organization.get(hrefParserService.idFromHref(params.organization))
        if(params.organization && !organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        def list = Extension.createCriteria().list {
            if(organization) {
                owner {
                    eq('organization', organization)
                }
            }
        }

        def results = list.collect {
            return [
                'number': it.number,
                'ip': it.ip
            ]
        }

        def json = [
            count: results.size(),
            results: results
        ]

        render json as JSON
    }
}
