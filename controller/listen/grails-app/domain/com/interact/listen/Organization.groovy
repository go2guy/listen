package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.util.Holders

class Organization {
    String contextPath
    boolean enabled = true
    Set enabledFeatures = []
    String name
    String outboundCallid
    boolean outboundCallidByDid = false
    int extLength = Holders.config.com.interact.listen.organization.defaultExtLength
    String adServer;
    String adDomain;
    String ldapBasedn;
    String ldapPort;
    String ldapDc;
    String route;
    String apiKey;
    boolean postCdr;
    String cdrUrl;

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
//        contextPath blank: false, maxSize: 50, unique: true, matches: '^[a-z0-9_-]+$', notEqual: 'custodian'
        contextPath blank: false, maxSize: 50, unique: true, notEqual: 'custodian'
        name blank: false, maxSize: 100, unique: true // TODO for max constraints, add maxlengths to the text fields on the views
        outboundCallid blank: false, maxSize: 10, unique: false, matches: '^[0-9]+$'
        extLength min: 2, max: 9
        adServer nullable: true
        adDomain nullable: true
        ldapBasedn nullable: true
        ldapPort nullable: true
        ldapDc nullable: true
        route nullable: true
        cdrUrl nullable: true
        apiKey nullable: true
    }

    String toString() {
        name
    }

    String attendantPromptDirectory()
    {
        return "/interact/listen/artifacts/" + id + "/attendant/";
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
