package com.interact.listen

import org.joda.time.DateTime
import org.joda.time.Duration

class Audio {
    DateTime dateCreated // auto-timestamped by GORM
    String description
    Duration duration
    String fileSize
    DateTime lastUpdated // auto-timestamped by GORM
    String transcription = ''
    String uri

    static constraints = {
        description nullable: true, blank: false, maxSize: 200
        fileSize blank: false, maxSize: 20
        transcription blank: true, maxSize: 4000
        uri blank: false, maxSize: 500
    }

    String detectContentType()
    {
        if(uri.indexOf(".") >= 0)
        {
            String extension = uri.substring(uri.lastIndexOf(".") + 1);
            if(extension == 'wav')
            {
                return "audio/x-wav";
            }

            if(extension == 'mp3')
            {
                return "audio/mpeg";
            }
        }

        return "audio/x-wav";
    }

    String mp3Uri() {
        if(uri.endsWith('.wav')) {
            return uri.replace('.wav', '.mp3')
        }

        if(!uri.endsWith('.mp3')) {
            return uri.concat('.mp3')
        }

        return uri
    }
}
