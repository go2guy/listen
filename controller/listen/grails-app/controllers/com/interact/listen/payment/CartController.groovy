package com.interact.listen.payment

import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class CartController {
    static allowedMethods = [
        authorize: 'POST',
        cancelCheckout: 'POST',
        checkout: 'GET'
    ]

    def paymentService

    def authorize = { PaymentCommand payment ->
        if(!payment.validate()) {
            render(view: 'checkout', model: [payment: payment])
            return
        }

        def result = paymentService.authCapture(payment, new BigDecimal('0.01'))
        log.debug "Authorize.net result: response code = [${result.responseCode}], reason [${result.reasonResponseCode}]"

        if(result.isApproved()) {
            flash.successMessage = 'Transaction successful'
            redirect(action: 'checkout')
        } else {
            flash.errorMessage = 'An error occurred accepting your payment'
            render(view: 'checkout')
        }
    }

    def cancelCheckout = {
        redirect(action: 'checkout')
    }

    def checkout = {
        render(view: 'checkout')
    }
}
