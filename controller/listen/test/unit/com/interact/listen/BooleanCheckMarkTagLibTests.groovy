package com.interact.listen

import grails.test.TagLibUnitTestCase
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class BooleanCheckMarkTagLibTests extends TagLibUnitTestCase {
    BooleanCheckMarkTagLibTests() {
        super(BooleanCheckMarkTagLib)
    }

    // null value throws error
    void testCheckMark0() {
        try {
            tagLib.checkMark(value: null)
        } catch(GrailsTagException e) {
            assertEquals 'Tag [checkMark] is missing required attribute [value]', e.message
        }
    }

    // true value outputs unicode checkmark
    void testCheckMark1() {
        tagLib.checkMark(value: true)
        assertEquals '&#10003;', tagLib.out.toString()
    }

    // false value outputs nothing
    void testCheckMark2() {
        tagLib.checkMark(value: false)
        assertEquals '', tagLib.out.toString()
    }
}
