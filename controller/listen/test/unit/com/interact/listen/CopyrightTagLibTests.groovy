package com.interact.listen

import grails.test.TagLibUnitTestCase
import org.joda.time.DateTime

class CopyrightTagLibTests extends TagLibUnitTestCase {
    CopyrightTagLibTests() {
        super(CopyrightTagLib)
    }

    // outputs copyright
    void testCopyright0() {
        final def thisYear = new DateTime().year
        tagLib.copyright()
        assertEquals "Listen &copy;2010-${thisYear} Interact Incorporated, <a href='http://www.interactincorporated.com' title='Interact Incorporated'>interactincorporated.com</a>", tagLib.out.toString()
    }
}
