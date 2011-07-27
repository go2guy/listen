package com.interact.listen.attendant

import com.interact.listen.Organization
import org.joda.time.LocalDate

class MenuLocatorService {
    // for the provided organization, finds the appropate entry menu
    // for the current date and time.
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

        // is there a holiday configured for the group?
        def override = PromptOverride.findByOverridesMenuAndDate(group, new LocalDate())
        if(override) {
            group = override.useMenu
        }

        def menu = Menu.findByIsEntryAndMenuGroup(true, group)
        if(!menu) {
            log.warn "No entry menu configured for default MenuGroup for organization [${organization}]"
        }

        return [menu: menu, override: override]
    }
}
