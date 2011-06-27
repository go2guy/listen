package com.interact.listen.pbx.findme

import com.interact.listen.pbx.Extension
import com.interact.listen.User

class FindMeNumber {
    Integer dialDuration
    Boolean isEnabled = true
    String number
    Integer priority
    User user

    static belongsTo = User

    static constraints = {
        dialDuration min: 1, max: 60
        number blank: false, maxSize: 50
        priority min: 0, max: 1000
    }

    static def findAllByUserGroupedByPriority(User user, includeDisabled = true) {
        def groups = [:] as TreeMap
        def numbers = includeDisabled ? FindMeNumber.findAllByUser(user) : FindMeNumber.findAllByUserAndIsEnabled(user, true)
        numbers.each { number ->
            if(groups[number.priority] == null) {
                groups.put(number.priority, [])
            }
            groups[number.priority] << number
        }

        // we dont want the keys in our returned object, just a list of lists
        return groups.collect { entry -> entry.value }
    }

    static def removeAll(User user) {
       executeUpdate 'DELETE FROM FindMeNumber WHERE user=:user', [user: user]
    }

    def forwardedTo() {
        return Extension.findByOwnerAndNumber(user, number)?.forwardedTo
    }
}
