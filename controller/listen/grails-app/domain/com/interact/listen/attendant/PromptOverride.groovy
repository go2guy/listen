package com.interact.listen.attendant

import com.interact.listen.Organization
import org.joda.time.LocalDate

class PromptOverride {
    LocalDate date
    String optionsPrompt

    static belongsTo = [menuGroup: MenuGroup]

    static constraints = {
        date unique: 'menuGroup', validator: { val ->
            val?.isBefore(new LocalDate()) ? 'before.today' : true
        }
        optionsPrompt blank: false
    }

    static def findAllByOrganizationAndNotPast(Organization organization, def params = [:]) {
        def today = new LocalDate()
        def c = PromptOverride.createCriteria()
        return c.list(params) {
            ge('date', today)
            menuGroup {
                eq('organization', organization)
            }
        }
    }
}
