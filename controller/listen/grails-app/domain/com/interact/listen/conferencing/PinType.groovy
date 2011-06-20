package com.interact.listen.conferencing

enum PinType {
    ACTIVE,
    ADMIN,
    PASSIVE;

    String displayName() {
        name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase()
    }

    static PinType fromDisplayName(def displayName) {
        try {
            PinType.valueOf(displayName.toUpperCase())
        } catch(NullPointerException e) {
            throw new IllegalArgumentException(e)
        }
    }
}
