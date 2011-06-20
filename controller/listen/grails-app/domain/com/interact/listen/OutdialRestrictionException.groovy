package com.interact.listen

class OutdialRestrictionException {
    User target

    static belongsTo = [restriction: OutdialRestriction]
}
