package com.interact.listen

import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute
import org.apache.log4j.Logger

class CallRoutingService {
    static transactional = false

    def routeCall(def ani, def dnis) {
        log.debug "route call request for [${ani}][${dnis}]"
        //Check external number routes first
        def mappings = [:]
        def organization
        NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL).each {
            mappings.put(it.pattern, it)
        }

        def matcher = new WildcardNumberMatcher()
        def mapping = matcher.findMatch(dnis, mappings)
        def extension
        if(mapping) {
            log.debug "returning because of mappings"
            return [application: mapping.destination, organization: mapping.organization]
        } else {
            //Look up organization by the extension that is making the call
            extension = Extension.findByNumber(ani)
            organization = extension?.owner?.organization
        }        

        if(organization == null) {
            //Unknown ip dialed a non-external route.  We do not know what organization to use here, so fail
            log.error "call with unknown organization"
            return null
        }

        mappings.clear()
        NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.INTERNAL).each {
                mappings.put(it.pattern, it)
        }

        mapping = matcher.findMatch(dnis, mappings)

        if(mapping) {
            log.debug "returning final with mappings"
            return [application: mapping.destination, organization: organization]
        } else {
            log.debug "Returning without application mappings"
            return null
        }
    }
    
    def determineOutboundCallId(def user) {
        log.debug ("Determine outbound caller id for user [${user?.realName}] with org [${user?.organization?.name}]")
        
        if(user?.organization?.outboundCallidByDid){
            def did = DirectInwardDialNumber.findByOwner(user)
            if(did){
                log.debug "Setting outbound callid by DID [${did?.number}]"
                return did?.number
            } else {
                log.debug "extension [${user?.realName}] does not have a did"
                return user?.organization?.outboundCallid
            }
        } else {
            log.debug "Setting outbound callid by organizational default"
            return user?.organization?.outboundCallid
        }
        return null
    }
}
