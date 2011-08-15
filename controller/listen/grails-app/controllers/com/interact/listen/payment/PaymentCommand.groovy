package com.interact.listen.payment

class PaymentCommand {
    String cardNumber
    String cardholderName
    int cardExpirationMonth
    int cardExpirationYear
    String cardVerification

    static constraints = {
        cardNumber blank: false
        cardholderName blank: false
        cardExpirationMonth inList: 1..12
        cardExpirationYear inList: 2011..2020
        cardVerification blank: false
    }

    def normalizedCardNumber() {
        cardNumber.replaceAll(' ', '').replaceAll('-', '')
    }
}
