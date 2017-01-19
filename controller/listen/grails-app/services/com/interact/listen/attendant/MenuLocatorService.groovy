package com.interact.listen.attendant

import com.interact.listen.Organization
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

class MenuLocatorService {
    // for the provided organization, finds the appropriate entry menu for the current date and time.
    def findEntryMenu(Organization organization) {

        def group = MenuGroup.findAllByOrganizationAndIsDefault(organization, false).find {
            // a group with no restrictions never applies
            if(it.restrictions.size() == 0) {
                return false
            }

            return it.restrictions.any { it.appliesToNow() }
        }

        // no group? use the default
        if(!group) {
            group = MenuGroup.findByOrganizationAndIsDefault(organization, true)
        }

        LocalDateTime currentTime = new LocalDateTime();

        // Is there an event going on?
        def c = PromptOverride.createCriteria()
        List<PromptOverride> override = c.listDistinct() {
            le('startDate', currentTime)
            ge('endDate', currentTime)
            useMenu
                    {
                        eq('organization', organization)
                    }
            order 'eventType', 'asc'
        }

        if(override && override.size() > 0)
        {
            group = override.get(0).useMenu
        }

        def menu = Menu.findByIsEntryAndMenuGroup(true, group)
        if(!menu) {
            log.warn "No entry menu configured for default MenuGroup for organization [${organization}]"
        }

        return [menu: menu, override: override]
    }
}
