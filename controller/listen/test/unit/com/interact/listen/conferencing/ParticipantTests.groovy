package com.interact.listen.conferencing

import com.interact.listen.User

class ParticipantTests extends GroovyTestCase {

    // display name uses realName + ani if available, otherwise ani
    void testDisplayName0() {
        final def realName = 'Rutherford B. Hayes'
        final def ani = '1800BEEHAZE'
        final def user = new User(realName: realName)
        final def participant = new Participant(ani: ani, user: user)
        assertEquals "${realName} (${ani})", participant.displayName()

        participant.user = null
        assertEquals ani, participant.displayName()

        user.realName = null
        participant.user = user
        assertEquals ani, participant.displayName()
    }
}
