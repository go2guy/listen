package com.interact.listen

import com.interact.listen.license.ListenFeature
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration

import grails.plugin.springsecurity.SpringSecurityUtils

class ButtonMenuTagLib {
    static namespace = 'listen'

    def licenseService
    def springSecurityService
    def inboxMessageService

    def buttonMenu = { attrs ->
        if(!attrs.tab) return
        def user = springSecurityService.getCurrentUser()

        switch(attrs.tab) {
            case 'administration':

                out << '<ul class="button-menu">'
                button(attrs.button == 'routing', 'administration', 'routing', 'button.menu.administration.routing')
                button(attrs.button == 'phones', 'administration', 'listPhones', 'button.menu.administration.phones')
                button(attrs.button == 'outdialing', 'administration', 'outdialing', 'button.menu.administration.outdialing')
                button(attrs.button == 'configuration', 'administration', 'configuration', 'button.menu.administration.configuration')
                if(licenseService.canAccess(ListenFeature.ACD)) {
                    button(attrs.button == 'skills', 'administration', 'skills', 'button.menu.administration.skills')
                }
//                button(attrs.button == 'android', 'administration', 'android', 'button.menu.administration.android')
                button(attrs.button == 'history', 'administration', 'history', 'button.menu.administration.history')
                button(attrs.button == 'reports', 'reports', 'list', 'button.menu.administration.reports')
                out << '</ul>'

                break

            case 'acd':
                out << '<ul class="button-menu">'

                button(attrs.button == 'status', 'acd', 'status', 'button.menu.acd.status')
                if( SpringSecurityUtils.ifAnyGranted('ROLE_ORGANIZATION_ADMIN,ROLE_QUEUE_USER') )
                {
                    button(attrs.button == 'callQueue', 'acd', 'callQueue', 'button.menu.acd.callQueue')
                }
                if ( SpringSecurityUtils.ifAnyGranted('ROLE_VOICEMAIL_USER,ROLE_FAX_USER') ) {
                  /* Can't just call button for this one since we have to add a custom span element for the message count */
                  out << '<li class="' + (attrs.button == 'inbox' ? 'current' : '') + '">'
                  out << g.link(controller: 'messages', action: 'acdInbox') { g.message(code: 'button.menu.acd.acdInbox') + '<span id="new-acd-message-count">' +
                         inboxMessageService.newAcdMessageCount() + '</span>' }
                  out << '</li>'
                }

                button(attrs.button == 'callHistory', 'acd', 'callHistory', 'button.menu.acd.callHistory')

                if( SpringSecurityUtils.ifAnyGranted('ROLE_ORGANIZATION_ADMIN') )
                {
                    button(attrs.button == 'agentStatus', 'acd', 'agentStatus', 'button.menu.acd.agentStatus')
                }
                out << '</ul>'

                break

            case 'attendant':

                out << '<ul class="button-menu">'
                button(attrs.button == 'menu', 'attendant', 'menu', 'button.menu.attendant.menu')
                button(attrs.button == 'holidays', 'attendant', 'holidays', 'button.menu.attendant.holidays')
                button(attrs.button == 'unscheduledEvent', 'attendant', 'unscheduledEvent', 'button.menu.attendant.unscheduledEvent')
                out << '</ul>'

                break

            case 'conferencing':

                out << '<ul class="button-menu">'
                button(attrs.button == 'manage', 'conferencing', 'manage', 'button.menu.conferencing.manage')
                button(attrs.button == 'invitations', 'conferencing', 'invitations', 'button.menu.conferencing.invitations')
                button(attrs.button == 'recordings', 'conferencing', 'recordings', 'button.menu.conferencing.recordings')
                out << '</ul>'

                break

            case 'custodianAdministration':

                out << '<ul class="button-menu">'
                button(attrs.button == 'outdialing', 'custodianAdministration', 'outdialing', 'button.menu.custodianAdministration.outdialing')
                button(attrs.button == 'mail', 'custodianAdministration', 'mail', 'button.menu.custodianAdministration.mail')
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
                    //if(afterHours?.mobilePhone) {
                    if (afterHours) {
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
                button(attrs.button == 'permissions', 'user', 'permissions', 'button.menu.users.permissions')
                out << '</ul>'

                break

            case 'messages':
                // falls through
            case 'voicemail':
                // falls through
            case 'fax':

                out << '<ul class="button-menu">'
                button(attrs.button == 'inbox', 'messages', 'inbox', 'button.menu.messages.inbox')
                if(licenseService.canAccess(ListenFeature.FAX)) {
                    button(attrs.button == 'sendfax', 'fax', 'create', 'button.menu.messages.sendfax')
                }
                if(licenseService.canAccess(ListenFeature.VOICEMAIL)) {
                    button(attrs.button == 'settings', 'voicemail', 'settings', 'button.menu.messages.settings')
                }
                out << '</ul>'

                break

            default:
                out << '<ul class="button-menu"></ul>'
        }
    }

    def button(boolean current, String controller, String action, String code) {
        out << '<li class="' + (current ? 'current' : '') + '">'
        out << g.link(controller: controller, action: action) { g.message(code: code) }
        out << '</li>'
    }
}
