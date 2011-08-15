package com.interact.listen.purchase

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class CartController {
    static allowedMethods = [
        authorize: 'POST',
        checkout: 'GET'
    ]

    def authorize = {
        // TODO authcapture service
    }

    def cancelCheckout = {
        redirect(action: 'checkout')
    }

    def checkout = {
        render(view: 'checkout')
    }
}
