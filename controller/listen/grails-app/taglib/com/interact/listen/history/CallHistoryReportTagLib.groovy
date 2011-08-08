package com.interact.listen.history

class CallHistoryReportTagLib {
    static namespace = 'listen'

    def reportCount = { attrs ->
        def value = attrs.value
        if(!value || value == 0) {
            value = '-'
        } 
        out << value
    }

    def reportDuration = { attrs ->
        if(!attrs.duration || attrs.duration.millis == 0) {
            out << '-'
        } else {
            out << listen.formatduration(duration: attrs.duration, zeroes: false, millis: false)
        }
    }
}
