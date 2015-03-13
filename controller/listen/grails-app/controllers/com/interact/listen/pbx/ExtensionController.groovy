package com.interact.listen.pbx

import com.interact.listen.*
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

//import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_SPOT_API'])
class ExtensionController {

    static allowedMethods = [
        getExtensionsByOrganization: 'GET'
    ]

    def hrefParserService

    def getExtensionsByOrganization = {
        log.debug("getExtensionsByOrganization called with params [${params}]")

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
            def phoneType = ""
            if(it?.sipPhone?.userAgent) {
                phoneType = it?.sipPhone?.userAgent.split(' ')[0]
            }
            return [
                'number': it.number,
                //'ip': it?.sipPhone?.ip,
                'phoneType': phoneType
            ]
        }

        def json = [
            count: results.size(),
            results: results
        ]

        render json as JSON
    }
}
