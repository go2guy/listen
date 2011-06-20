package com.interact.listen

class BooleanCheckMarkTagLib {
    static namespace = 'listen'

    def checkMark = { attrs ->
        if(attrs.value == null) throwTagError 'Tag [checkMark] is missing required attribute [value]'
        if(attrs.value) out << '&#10003;'
    }
}
