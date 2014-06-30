package com.interact.listen.voicemail

import com.interact.listen.Audio
import com.interact.listen.InboxMessage

class Voicemail extends InboxMessage {
    String ani
    Audio audio

    static constraints = {
        // all properties must be nullable (inheritance)
        ani nullable: true, blank: false
        audio nullable: true
    }
    
    static mapping = {
        audio cascade: 'delete'
    }
}
