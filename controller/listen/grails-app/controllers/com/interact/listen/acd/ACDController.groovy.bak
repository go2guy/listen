package com.interact.listen.acd

import grails.plugins.springsecurity.Secured

/* @Secured(['ROLE_ACD_USER']) */
class ConferencingController {
    /* static allowedMethods = [ */
        /* index: 'GET' */
    /* ] */

    def index = {
        /* def user = authenticatedUser */
        log.debug("Entered index...redirecting to status...")
        redirect(action: 'status')
    }

    def updateStatus = {
        /* filler for now... */
        redirect(action: 'status')
    }

    def status = {
        log.debug("Displaying status...maybe...")
        render(view: 'status')
    }
}
