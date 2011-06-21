package com.interact.listen

import org.joda.time.LocalTime
import org.joda.time.format.PeriodFormatterBuilder

class DateTimeTagLib {
    static namespace = 'listen'

    def prettyduration = { attrs ->
        if(!attrs.duration) throwTagError 'Tag [prettyduration] is missing required attribute [duration]'
        def formatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendHours()
            .appendSuffix('h ')
            .printZeroNever()
            .appendMinutes()
            .appendSuffix('m ')
            .printZeroRarelyLast()
            .minimumPrintedDigits(1)
            .appendSeconds()
            .appendSuffix('s')
            .toFormatter()
        out << formatter.print(attrs.duration.toPeriod())
    }

    def timePicker = { attrs ->
        def name = attrs.name
        def id = attrs.id ?: name
        def value = attrs.value
        if(value == 'none') {
            value = null
        } else if(!value) {
            value = new LocalTime()
        }

        out << '<input type="hidden" name="' + name + '" value="struct"/>'
        out << '<select name="' + name + '_hour" id="' + id + '_hour"/>'
        for(i in 0..23) {
            def h = i.toString().padLeft(2, '0')
            out << '<option value="' + h + '"' + (value?.hourOfDay == i ? ' selected="selected"' : '') + '>' + h + '</option>'
        }
        out << '</select>'


        out << '<select name="' + name + '_minute" id="' + id + '_minute"/>'
        for(i in [0, 15, 30, 45]) {
            def m = i.toString().padLeft(2, '0')
            out << '<option value="' + m + '"' + (value?.minuteOfHour == i ? ' selected="selected"' : '') + '>' + m + '</option>'
        }
        out << '</select>'
    }
}