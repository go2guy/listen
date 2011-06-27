package com.interact.listen.voicemail.afterhours

import com.interact.listen.MobilePhone
import com.interact.listen.Organization

class AfterHoursConfiguration {
    String alternateNumber
    Organization organization
    MobilePhone mobilePhone
    String realizeAlertName
    String realizeUrl

    static constraints = {
        alternateNumber blank: true, email: true
        mobilePhone nullable: true
        realizeAlertName blank: true
        realizeUrl blank: true
    }

    def alternateNumberComponents() {
        if(alternateNumber?.contains('@')) {
            def s = alternateNumber.split('@')
            return [
                number: s[0],
                provider: s[1]
            ]
        }
        return [
            number: alternateNumber,
            provider: ''
        ]
    }
}
