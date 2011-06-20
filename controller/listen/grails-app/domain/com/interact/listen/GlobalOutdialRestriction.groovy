package com.interact.listen

class GlobalOutdialRestriction {
    String pattern

    static constraints = {
        pattern blank: false, maxSize: 50 // TODO pattern validation
    }
}
