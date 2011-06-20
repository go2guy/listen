package com.interact.listen

enum PhoneNumberType {
    EXTENSION(true),
    HOME(false),
    MOBILE(false),
    OTHER(false),
    VOICEMAIL(true);

    boolean system

    private PhoneNumberType(boolean system){
        this.system = system
    }

    def isSystem() {
        system
    }

    def getKey() { name() } // used for populating select list keys in the view

    String toString() {
        def name = name().toLowerCase()
        return name[0].toUpperCase() + name[1..-1]
    }

    static def systemTypes() {
        PhoneNumberType.values().findAll { it.system }
    }

    static def userTypes() {
        PhoneNumberType.values().findAll { !it.system }
    }
}
