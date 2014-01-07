package com.interact.listen.conferencing

import com.interact.listen.Organization
import grails.util.Holders

class ConferencingConfiguration
{
    Organization organization
    int pinLength = Holders.config.com.interact.listen.conferencing.defaultPinLength

    static constraints =
    {
        pinLength min: 3, max: 32
    }
}
