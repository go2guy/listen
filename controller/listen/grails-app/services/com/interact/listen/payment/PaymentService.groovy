package com.interact.listen.payment

import net.authorize.Environment
import net.authorize.Merchant
import net.authorize.TransactionType
import net.authorize.aim.Transaction
import net.authorize.data.creditcard.CreditCard

class PaymentService {
    def grailsApplication

    def authCapture(PaymentCommand payment, BigDecimal amount) {
        if(!payment.validate()) {
            throw new IllegalArgumentException('Incorrect payment information')
        }

        final def loginId = grailsApplication.config.com.interact.listen.authorizenet.loginId
        final def transactionKey = grailsApplication.config.com.interact.listen.authorizenet.transactionKey

        def merchant = Merchant.createMerchant(Environment.SANDBOX, loginId, transactionKey)

        def card = CreditCard.createCreditCard()
        card.creditCardNumber = payment.normalizedCardNumber()
        card.expirationMonth = payment.cardExpirationMonth
        card.expirationYear = payment.cardExpirationYear
        card.cardCodeVerification = payment.cardVerification

        def transaction = merchant.createAIMTransaction(TransactionType.AUTH_CAPTURE, amount)
        transaction.creditCard = card

        return merchant.postTransaction(transaction)
    }
}
