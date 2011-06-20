package com.interact.listen.attendant

import com.interact.listen.Organization

class MenuLocatorService {
    static scope = 'singleton'
    static transactional = true

    // for the provided organization, finds the appropate entry menu
    // for the current date and time.
    def findEntryMenu(Organization organization) {
        def nonDefault = MenuGroup.findAllByOrganizationAndIsDefault(organization, false)
        for(def group : nonDefault) {
            if(group.restrictions.size() == 0) {
                // a group with no restrictions never applies
                continue
            }

            if(!group.restrictions.any { it.appliesToNow() }) {
                continue
            }

            def entryMenu = Menu.findByIsEntryAndMenuGroup(true, group)
            if(!entryMenu) {
                log.warn "Menu group [${group}] matched time restriction, but it has no entry menu"
                continue
            }

            return entryMenu
        }

        def group = MenuGroup.findByOrganizationAndIsDefault(organization, true)
        def entryMenu = Menu.findByIsEntryAndMenuGroup(true, group)
        if(!entryMenu) {
            log.warn "No entry menu configured for default MenuGroup for organization [${organization}]"
        }
        return entryMenu
    }
}
