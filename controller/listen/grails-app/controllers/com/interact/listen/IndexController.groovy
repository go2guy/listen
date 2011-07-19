package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils as SSU

@Secured(['IS_AUTHENTICATED_FULLY'])
class IndexController {
    def licenseService // injected

    // routes a logged-in user to a relevant page
    def index = {

        // trying to go for conciseness/readability here with the formatting
        // TODO i think we can try and find a better way to do this routing (i.e. not with controller)

        // profile is a good fallback, it will always be there
        def controller = 'profile'

        // TODO make this use the new roles
        if(has('ROLE_CUSTODIAN')) {
            controller = 'organization'
        } else if((has('ROLE_VOICEMAIL_USER') && can(ListenFeature.VOICEMAIL)) ||
                  (has('ROLE_FAX_USER') && can(ListenFeature.FAX))) {
            controller = 'messages'
        } else if(can(ListenFeature.CONFERENCING)) {
            controller = 'conferencing'
        } else if (can(ListenFeature.FINDME)) {
            controller = 'findme'
        }

        redirect(controller: controller)
    }

    private has(def authority) {
        return SSU.ifAllGranted(authority)
    }

    private can(def feature) {
        return licenseService.canAccess(feature)
    }
}
