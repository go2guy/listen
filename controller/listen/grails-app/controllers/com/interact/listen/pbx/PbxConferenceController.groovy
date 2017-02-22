package com.interact.listen.pbx

import com.interact.listen.*
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

//import grails.plugins.springsecurity.Secured
// import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_SPOT_API'])
class PbxConferenceController {

    // static allowedMethods = [
        // registerPbxConference: 'POST',
        // getPbxConference: 'GET',
        // removePbxConference: 'DELETE',
        // updatePbxConference: 'PUT'
    // ]

    def pbxConferenceService

    def registerPbxConference = {
        def jsonParams = JSON.parse(request)
        log.debug "registerPbxConference with params [${jsonParams}]"
        def json = [:]
        // json.success = pbxConferenceService.create(params)
        def conference = pbxConferenceService.create(jsonParams)
        if ( conference ) {
            json.success = true
            json.id = conference.id
            json.name = conference.name
            json.monitoredExtension = conference.monitoredExtension
            json.monitoringSession = conference.monitoringSession
        }
        log.debug "Returning json [${json as JSON}]"
        render json as JSON
    }

    def updatePbxConference = {
        def jsonParams = JSON.parse(request)
        jsonParams.id = params.id
        log.debug "updatePbxConference with params [${jsonParams}]"
        // def test = JSON.parse(request)
        // log.debug "test[${test as JSON}]"
        def conference = pbxConferenceService.update(jsonParams)
        def json = [:]
        if ( conference ) {
            json.success = true
            json.id = conference.id
            json.name = conference.name
            json.monitoredExtension = conference.monitoredExtension
            json.monitoringSession = conference.monitoringSession
        }
        log.debug "Returning json [${json as JSON}]"
        render json as JSON
    }

    def getPbxConference = {
        log.debug "getConference with params [${params}]"
        def conference
        def json = [:]
        json.success = false

        if ( !params.extension && !params.name ) {
            log.debug "Missing required params: [extension/name]"
        }

        if ( params.name ) {
            log.debug "Attempting to retrieve pbx conference via NAME[${params.name}]"
            conference = PbxConference.findByName(params.name)
        }
        else {
            log.debug "Attempting to retrieve pbx conference via ANI[${params.extension}]"
            conference = PbxConference.findByAni(params.extension)

            if ( !conference ) {
                log.debug "Attempting to retrieve pbx conference via DNIS[${params.extension}]"
                conference = PbxConference.findByDnis(params.extension)
            }
        }

        if ( conference ) {
            json.success = true
            json.name = conference.name
            json.id = conference.id
            json.monitoredExtension = conference.monitoredExtension
            json.monitoringSession = conference.monitoringSession
        }

        log.debug "Returning json [${json as JSON}]"
        render json as JSON
    }

    def removePbxConference = {
        log.debug "removeConference with params [${params}]"
        def json = [:]
        json.success = false
        // if ( !params.name ) {
            // log.debug "Missing required parameters: [name]"
        // }

        if ( !params.id ) {
            log.debug "Missing required parameters: [id]"
        }

        if ( params.id == "0" ) {
            log.debug "Invalid id [0]"
        }
        
        // def conference = PbxConference.findByName(params.name)
        def conference = PbxConference.get(Integer.parseInt(params.id))
        
        try {
            conference.delete(flush: true)
            json.success = true
        }
        catch(Exception e) {
            log.error("Exception deleting pbxConference[${params.id}]: " + e.getMessage(), e)
        }
        // if ( conference.delete(flush: true) ) {
            // log.debug "Successfully removed pbx conference [${params.id}]"
            // json.sucess = true
        // }

        render json as JSON
    }

}
