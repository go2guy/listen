package com.interact.listen.history

class CallHistoryReportTagLib {
    static namespace = 'listen'

    def reportCount = { attrs ->
        def value = attrs.value
        if(value && value > 0) {
            out << value
        }
    }

    def reportDuration = { attrs ->
        if(attrs.duration && attrs.duration.millis > 0) {
            out << listen.formatduration(duration: attrs.duration, zeroes: false, millis: false)
        }
    }
}
