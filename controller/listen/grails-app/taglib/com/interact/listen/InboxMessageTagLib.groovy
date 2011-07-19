package com.interact.listen

import com.interact.listen.fax.Fax
import com.interact.listen.voicemail.Voicemail

class InboxMessageTagLib {
    static namespace = 'listen'

    def ifIsVoicemail = { attrs, body ->
        if(!attrs.message) throwTagError 'Tag [ifIsVoicemail] is missing required attribute [message]'

        if(attrs.message.instanceOf(Voicemail)) {
            out << body()
        }
    }

    def ifIsFax = { attrs, body ->
        if(!attrs.message) throwTagError 'Tag [ifIsFax] is missing required attribute [message]'

        if(attrs.message.instanceOf(Fax)) {
            out << body()
        }
    }
}
