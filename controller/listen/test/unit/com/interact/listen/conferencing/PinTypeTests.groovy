package com.interact.listen.conferencing

class PinTypeTests extends GroovyTestCase {
    // successful: test all values with displayName()
    void testDisplayName0() {
        assertEquals 'Active', PinType.ACTIVE.displayName()
        assertEquals 'Admin', PinType.ADMIN.displayName()
        assertEquals 'Passive', PinType.PASSIVE.displayName()
    }

    // successful: test all valid fromDisplayName() values
    void testFromDisplayName0() {
        assertEquals PinType.ACTIVE, PinType.fromDisplayName('Active')
        assertEquals PinType.ADMIN, PinType.fromDisplayName('Admin')
        assertEquals PinType.PASSIVE, PinType.fromDisplayName('Passive')
    }

    // failure: fromDisplayName() with unknown name or null name yields exception
    void testFromDisplayName1() {
        shouldFail(IllegalArgumentException) {
            PinType.fromDisplayName(String.valueOf(System.currentTimeMillis()))
        }
        shouldFail(IllegalArgumentException) {
            PinType.fromDisplayName(null)
        }
    }
}
