package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.plugin.springsecurity.annotation.Secured
//import grails.plugins.springsecurity.Secured
import grails.plugin.springsecurity.SpringSecurityUtils as SSU

@Secured(['IS_AUTHENTICATED_FULLY'])
class IndexController {
    def licenseService

    // routes a logged-in user to a relevant page
    def index = {

        // trying to go for conciseness/readability here with the formatting
        // TODO i think we can try and find a better way to do this routing (i.e. not with controller)

        // profile is a good fallback, it will always be there
        def controller = 'profile'
        String action = null;

        // TODO make this use the new roles
        if(has('ROLE_CUSTODIAN'))
        {
            controller = 'organization'
        }
        else if((has('ROLE_VOICEMAIL_USER') && can(ListenFeature.VOICEMAIL)) ||
                  (has('ROLE_FAX_USER') && can(ListenFeature.FAX)))
        {
            controller = 'messages';
            action = 'index';
        }
        else if(has('ROLE_ACD_USER'))
        {
            controller = 'acd';
            action = 'index';
        }
        else if(can(ListenFeature.CONFERENCING))
        {
            controller = 'conferencing'
        }
        else if (can(ListenFeature.FINDME))
        {
            controller = 'findme'
        }

        if(action != null)
        {
            redirect(controller: controller, action: action);
        }
        else
        {
            redirect(controller: controller);
        }
    }

    private has(def authority) {
        return SSU.ifAllGranted(authority)
    }

    private can(def feature) {
        return licenseService.canAccess(feature)
    }
}
