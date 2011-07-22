package com.interact.listen.conferencing

class PinTypeTests extends GroovyTestCase {
    // all values with displayName()
    void testDisplayName0() {
        assertEquals 'Active', PinType.ACTIVE.displayName()
        assertEquals 'Admin', PinType.ADMIN.displayName()
        assertEquals 'Passive', PinType.PASSIVE.displayName()
    }

    // all valid fromDisplayName() values
    void testFromDisplayName0() {
        assertEquals PinType.ACTIVE, PinType.fromDisplayName('Active')
        assertEquals PinType.ADMIN, PinType.fromDisplayName('Admin')
        assertEquals PinType.PASSIVE, PinType.fromDisplayName('Passive')
    }
}
