package com.interact.listen.conferencing

import com.interact.listen.Organization
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class ConferencingConfiguration {
    Organization organization
    int pinLength = CH.config.com.interact.listen.conferencing.defaultPinLength

    static constraints = {
        pinLength min: 3, max: 32
    }
}
