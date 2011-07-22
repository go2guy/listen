package com.interact.listen

class WildcardNumberMatcherTests extends GroovyTestCase {
    def matcher = new WildcardNumberMatcher()

    // find various matches within a list
    void testFindMatch0() {
        assertTrue matcher.findMatch('1', ['1'])
        assertTrue matcher.findMatch('1', ['*'])
        assertTrue matcher.findMatch('12', ['1*'])
        assertTrue matcher.findMatch('1', ['3', '2', '1'])

        assertFalse matcher.findMatch('1', ['3'])
    }

    // find various matches within a map
    void testFindMatch1() {
        assertEquals 'foo', matcher.findMatch('1', ['1': 'foo'])
        assertEquals 'foo', matcher.findMatch('1', ['*': 'foo'])
        assertEquals 'foo', matcher.findMatch('1', ['*': 'bar', '1': 'foo'])
        assertEquals 'bar', matcher.findMatch('234', ['*': 'bar', '1': 'foo'])
        assertEquals 'foo', matcher.findMatch('1234', ['123*': 'foo', '456*': 'bar'])

        assertNull matcher.findMatch('1', ['2': 'foo', '3': 'bar'])
    }
}
