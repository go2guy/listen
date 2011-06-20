package com.interact.listen.conferencing

class Pin {
    Conference conference
    String number // numeric string (to support leading zeroes)
    PinType pinType

    static belongsTo = Conference

    static constraints = {
        number blank: false, maxSize: 20, matches: '^[0-9]+$'
    }
}
