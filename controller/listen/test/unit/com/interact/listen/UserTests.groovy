package com.interact.listen

class UserTests extends GroovyTestCase {

    // friendly name returns real name if available
    void testFriendlyName0() {
        final def realName = 'Ulysses S. Grant'
        final def username = 'ulysses'
        final def user = new User(realName: realName, username: username)
        assertEquals realName, user.friendlyName()
    }

    // friendly name returns username if real name is not available
    void testFriendlyName1() {
        final def username = 'ulysses'
        final def user = new User(realName: null, username: username)
        assertEquals username, user.friendlyName()
    }
}
