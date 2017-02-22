package com.interact.listen.pbx

class PbxConferenceService {
    static transactional = false

    def create(def params) {
        log.debug "Attempting to register pbx conference with params [${params}]"

        PbxConference.withTransaction { status ->
            def conference = new PbxConference(params)

            log.debug "ani[${params.ani}]"

            if ( !(conference.validate() && conference.save()) ) {
                status.setRollbackOnly()
                log.debug "Encountered the following errors while registering conference: [${conference.errors}]"
                return false
            }
            return conference
        }
    }

    def update(def params) {
        log.debug "Attempting to update pbx conference with params [${params}]"

        PbxConference.withTransaction { status ->
            // def conference = new PbxConference(params)
            // def conference = PbxConference.findByName(params.name)
            def conference = PbxConference.get(params.id)

            log.debug "ani[${params.ani}]"
            log.debug "monitoringSession[${params.monitoringSession}]"

            // conference.
            conference.name = params.name ?: conference.name
            conference.ani = params.ani ?: conference.ani
            conference.dnis = params.dnis ?: conference.dnis
            conference.monitoringSession = params.monitoringSession ?: conference.monitoringSession
            conference.monitoredExtension = params.monitoredExtension ?: conference.monitoredExtension

            if ( !(conference.validate() && conference.save()) ) {
                status.setRollbackOnly()
                log.debug "Encountered the following errors while updating conference: [${conference.errors}]"
                return false
            }
            return conference
        }
    }
}
