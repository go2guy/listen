package com.interact.listen

import org.joda.time.DateTime

class CopyrightTagLib {
    static namespace = 'listen'

    def copyright = { attrs ->
        out << "Listen &copy;2010-${new DateTime().getYear()} NewNet Communication Technologies, <a href='http://www.newnet.com' title='NewNet'>newnet.com</a>"
    }
}
