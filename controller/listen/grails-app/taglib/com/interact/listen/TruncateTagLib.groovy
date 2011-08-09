package com.interact.listen

class TruncateTagLib {
    static namespace = 'listen'

    def truncate = { attrs ->
        if(!attrs.length) throwTagError 'Tag [truncate] is missing required attribute [length]'
        def length = attrs.length as int

        if(length < 0) throwTagError 'Tag [truncate] must have a non-negative [length] attribute'
        if(!attrs.value) return

        def value = attrs.value as String
        if(value.size() > length) {
            out << '<span title="' + value + '">' + value.substring(0, length) + '&hellip;</span>'
        } else {
            out << value
        }
    }
}
