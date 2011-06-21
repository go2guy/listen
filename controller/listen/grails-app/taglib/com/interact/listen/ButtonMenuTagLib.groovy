package com.interact.listen

import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration

class ButtonMenuTagLib {
    static namespace = 'listen'

    def springSecurityService // injected

    def buttonMenu = { attrs ->
        if(!attrs.tab) return
//        if(!attrs.button) throwTagError 'Tag [buttonMenu] is missing required attribute [button]'
        def user = springSecurityService.getCurrentUser()

        switch(attrs.tab) {
            case 'administration':

                out << '<ul class="button-menu">'
                button(attrs.button == 'routing', 'administration', 'routing', 'button.menu.administration.routing')
                button(attrs.button == 'outdialing', 'administration', 'outdialing', 'button.menu.administration.outdialing')
                button(attrs.button == 'phones', 'administration', 'phones', 'button.menu.administration.phones')
                button(attrs.button == 'configuration', 'administration', 'configuration', 'button.menu.administration.configuration')
                button(attrs.button == 'android', 'administration', 'android', 'button.menu.administration.android')
                button(attrs.button == 'history', 'administration', 'history', 'button.menu.administration.history')
                out << '</ul>'

                break

            case 'conferencing':
                out << '<ul class="button-menu">'
                button(attrs.button == 'manage', 'conferencing', 'manage', 'button.menu.conferencing.manage')
                button(attrs.button == 'scheduling', 'conferencing', 'scheduling', 'button.menu.conferencing.scheduling')
                button(attrs.button == 'recordings', 'conferencing', 'recordings', 'button.menu.conferencing.recordings')
                out << '</ul>'

                break

            case 'custodianAdministration':

                out << '<ul class="button-menu">'
                button(attrs.button == 'outdialing', 'custodianAdministration', 'outdialing', 'button.menu.custodianAdministration.outdialing')
                out << '</ul>'

                break

            case 'organization':

                out << '<ul class="button-menu">'
                button(attrs.button == 'list', 'organization', 'list', 'button.menu.organizations.list')
                button(attrs.button == 'create', 'organization', 'create', 'button.menu.organizations.create')
                button(attrs.button == 'routing', 'organization', 'routing', 'button.menu.organizations.routing')
                // TODO disable the number routing button if there are no organizations
                out << '</ul>'

                break

            case 'profile':

                out << '<ul class="button-menu">'
                button(attrs.button == 'settings', 'profile', 'settings', 'button.menu.profile.settings')
                if(!user.hasRole('ROLE_CUSTODIAN')) {
                    button(attrs.button == 'phones', 'profile', 'phones', 'button.menu.profile.phones')
                }

                if(user.organization) {
                    def afterHours = AfterHoursConfiguration.findByOrganization(user.organization)
                    if(afterHours?.phoneNumber) {
                        button(attrs.button == 'afterHours', 'profile', 'afterHours', 'button.menu.profile.afterHours')
                    }
                }

                button(attrs.button == 'history', 'profile', 'history', 'button.menu.profile.history')
                out << '</ul>'
                break

            case 'users':

                out << '<ul class="button-menu">'
                button(attrs.button == 'list', 'user', 'list', 'button.menu.users.list')
                button(attrs.button == 'create', 'user', 'create', 'button.menu.users.create')
                out << '</ul>'

                break

            case 'voicemail':

                out << '<ul class="button-menu">'
                button(attrs.button == 'inbox', 'voicemail', 'inbox', 'button.menu.voicemail.inbox')
                button(attrs.button == 'settings', 'voicemail', 'settings', 'button.menu.voicemail.settings')
                out << '</ul>'

                break
        }
    }

    private void button(boolean current, String controller, String action, String code) {
        out << '<li class="' + (current ? 'current' : '') + '">'
        out << g.link(controller: controller, action: action) { g.message(code: code) }
        out << '</li>'
    }
}