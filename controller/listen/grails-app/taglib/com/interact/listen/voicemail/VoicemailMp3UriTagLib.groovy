package com.interact.listen.voicemail

class VoicemailMp3UriTagLib {
    static namespace = 'listen'

    def mp3uri = { attrs ->
        def uri = attrs.voicemail.audio.uri

        if(uri.endsWith(".wav"))
        {
            uri = uri.replace(".wav", ".mp3");
        }
        
        if(!uri.endsWith(".mp3"))
        {
            uri.concat(".mp3");
        }

        out << "${uri}"
    }
}
