package com.interact.listen

import org.joda.time.DateTime

class CopyrightTagLib {
    static namespace = 'listen'

    def copyright = { attrs ->
        out << "Listen &copy;2010-${new DateTime().getYear()} Interact Incorporated, <a href='http://www.interactincorporated.com' title='Interact Incorporated'>interactincorporated.com</a>"
    }
}
