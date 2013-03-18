package com.interact.listen.conferencing

import com.interact.listen.Audio
import com.interact.listen.User
import org.joda.time.DateTime

class Participant {

    String ani // caller phone number
    Conference conference
    DateTime dateCreated
    boolean isAdmin = false
    boolean isAdminMuted = false
    boolean isMuted = false
    boolean isPassive = false
    Audio recordedName
    String sessionId
    User user

    static belongsTo = Conference

    static constraints = {
        ani blank: false, maxSize: 50, unique: 'conference'
        isAdminMuted validator: { val, obj ->
            return (obj.isAdmin && val ? 'cannot.admin.mute.admin' : true)
        }
        sessionId blank: false
        user nullable: true
    }

    def displayName() {
        user?.realName ? user?.realName + ' (' + ani + ')' : ani
    }

    def beforeInsert() {
        //The ani man come in the format of XXX(Y), get XXX as the actual ani
        def strippedAni = ani.tokenize('(')[0]
        user = User.lookupByPhoneNumber(strippedAni)
    }
}
