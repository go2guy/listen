package com.interact.listen.attendant

// TODO move TimeRestriction out of the voicemail package
import com.interact.listen.Organization
import com.interact.listen.voicemail.TimeRestriction

class MenuGroup {
    boolean isDefault = false
    String name

    static belongsTo = [organization: Organization]

    static hasMany = [
        menus: Menu,
        promptOverrides: PromptOverride,
        restrictions: TimeRestriction
    ]

    static fetchMode = [
        menus: 'eager',
        restrictions: 'eager'
    ]

    static mappedBy = [
        promptOverrides: 'overridesMenu'
    ]

    static constraints = {
        name blank: false, maxSize: 30
    }

    def menusInDisplayOrder() {
        return menus.sort { a, b ->
            if(a.isEntry != b.isEntry) {
                return a.name <=> b.name
            } else {
                return a.isEntry ? -1 : 1
            }
        }
    }
}
