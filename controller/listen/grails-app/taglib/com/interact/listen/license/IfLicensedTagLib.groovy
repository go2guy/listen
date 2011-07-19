package com.interact.listen.license
// TODO in the listen package for now; it would be nice to have the license library provide this instead

class IfLicensedTagLib {
    static namespace = 'listen'

    def springSecurityService
    def licenseService

    def ifLicensed = { attrs, body ->
        if(!attrs.feature) throwTagError('Tag [ifLicensed] is missing required attribute [feature]')
        
        def feature
        try {
            feature = ListenFeature.valueOf(attrs.feature)
        } catch(IllegalArgumentException e) {
            throwTagError("Tag [ifLicensed] was given a non-existent feature [${attrs.feature}]")
        }

        if(licenseService.isLicensed(feature)) {
            out << body()
        }
    }

    def ifNotLicensed = { attrs, body ->
        if(!attrs.feature) throwTagError('Tag [ifNotLicensed] is missing required attribute [feature]')

        def feature = ListenFeature.valueOf(attrs.feature)
        if(!licenseService.isLicensed(feature)) {
            out << body()
        }
    }

    def canAccess = { attrs, body ->
        if(!attrs.feature) throwTagError('Tag [canAccess] is missing required attribute [feature]')

        //Check if a feture is licensed first, if it is, check if it is enabled for the current organization
        def feature
        try {
            feature = ListenFeature.valueOf(attrs.feature)
        } catch(IllegalArgumentException e) {
            throwTagError("Tag [ifLicensed] was given a non-existent feature [${attrs.feature}]")
        }

        if(licenseService.canAccess(feature)) {
            out << body()
        }
    }

    def canAccessAny = { attrs, body ->
        if(!attrs.features) throwTagError('Tag [canAccessAny] is missing required attribute [features]')

        def names = attrs.features.split(',') as Set
        if(names.collect { ListenFeature.valueOf(it) }.any { licenseService.canAccess(it) }) {
            out << body()
        }
    }
}
