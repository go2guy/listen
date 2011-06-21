package com.interact.listen

@SuppressWarnings('ConfusingClassNamedException')
class OutdialRestrictionException {
    User target

    static belongsTo = [restriction: OutdialRestriction]
}
