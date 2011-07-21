package com.interact.listen

import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute

class CallRoutingService {
    static transactional = false

    def routeCall(def ani, def dnis) {
        //Check external number routes first
        def mappings = [:]
        def organization
        NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL).each {
            mappings.put(it.pattern, it)
        }

        def matcher = new WildcardNumberMatcher()
        def mapping = matcher.findMatch(dnis, mappings)

        if(mapping) {
            return [application: mapping.destination, organization: mapping.organization]
        } else {
            //Look up organization by the extension that is making the call
            organization = Extension.queryByIp(ani)?.owner.organization
        }        

        if(organization == null) {
            //Unknown ip dialed a non-external route.  We do not know what organization to use here, so fail
            return null
        }

        mappings.clear()
        NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.INTERNAL).each {
                mappings.put(it.pattern, it)
        }

        mapping = matcher.findMatch(dnis, mappings)

        if(mapping) {
            return [application: mapping.destination, organization: organization]
        }

        return null
    }
}
