package com.interact.listen.attendant

import com.interact.listen.Organization
import org.apache.log4j.Logger
import org.joda.time.LocalDate

class PromptOverride {
    private static final Logger _log = Logger.getLogger("grails.app.controllers.com.interact.listen.attendant.PromptOverride")

    LocalDate date
    String optionsPrompt
    MenuGroup useMenu

    static belongsTo = [overridesMenu: MenuGroup]

    static constraints = {
        date unique: 'overridesMenu', validator: { val, obj ->
            if(!obj.id) {
                val?.isBefore(new LocalDate()) ? 'before.today' : true
            }
        }
        optionsPrompt blank: false
    }

    static def findAllByOrganizationAndNotPast(Organization organization, def params = [:]) {
        def today = new LocalDate()
        _log.debug "Finding all PromptOverride instances with organization [${organization.name}] and params: ${params}"
        def c = PromptOverride.createCriteria()
        def result = c.listDistinct() {
            ge('date', today)
            overridesMenu {
                eq('organization', organization)
            }
            order 'date', 'asc'
        }
        _log.debug "  Found ${result.size()} PromptOverride instances"
        return result
    }
}
