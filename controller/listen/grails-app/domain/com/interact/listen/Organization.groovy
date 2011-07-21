package com.interact.listen

import com.interact.listen.license.ListenFeature

class Organization {
    String contextPath
    Set enabledFeatures = []
    String name

    static hasMany = [users: User, enabledFeatures: ListenFeature]

    static mapping = {
       enabledFeatures joinTable: [
            name: 'organization_enabled_features',
            key: 'organization_id',
            column: 'listen_feature',
            type: 'text'
        ]
    }


    static constraints = {
        contextPath blank: false, maxSize: 50, unique: true, matches: '^[a-z0-9_-]+$', notEqual: 'custodian'
        name blank: false, maxSize: 100, unique: true // TODO for max constraints, add maxlengths to the text fields on the views
    }

    String toString() {
        name
    }

    def attendantPromptDirectory() {
        return '/interact/listen/artifacts/attendant/' + id
    }

    Set addToEnabledFeatures(def enabledFeature) {
        enabledFeatures.add(enabledFeature)
        return enabledFeatures
    }

    Set removeFromEnabledFeatures(def enabledFeature) {
        enabledFeatures.remove(enabledFeature)
        return enabledFeatures
    }

    boolean hasTranscriptionEnabled() {
        def config = TranscriptionConfiguration.findByOrganization(this)
        if(!config) {
            return false
        }

        return config.isEnabled && config.phoneNumber.trim() != ''
    }
}
