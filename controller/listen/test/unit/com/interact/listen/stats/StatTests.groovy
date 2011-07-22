package com.interact.listen.stats

class StatTests extends GroovyTestCase {

    // value() is a 'LSTNCTL_XXXX' stat
    void testValue0() {
        assertEquals 'LSTNCTL_1004', Stat.GUI_LOGIN.value()
    }
}
