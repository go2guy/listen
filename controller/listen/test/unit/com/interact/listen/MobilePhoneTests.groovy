package com.interact.listen

class MobilePhoneTests extends GroovyTestCase {

    // smsEmail is a combination of number and smsDomain
    void testAsSmsEmail() {
        final def number = '1234567890'
        final def smsDomain = 'vtext.com'
        final def phone = new MobilePhone(number: number, smsDomain: smsDomain)
        assertEquals "${number}@${smsDomain}", phone.asSmsEmail()
    }
}
