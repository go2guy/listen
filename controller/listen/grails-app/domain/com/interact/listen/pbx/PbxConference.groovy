package com.interact.listen.pbx

class PbxConference {
    String name
    String ani
    String dnis
    String monitoringSession
    String monitoredExtension

    static constraints = {
        name nullable: false
        ani nullable: false
        dnis nullable: false
        monitoringSession nullable: true
        monitoredExtension nullable: true
    }
}
