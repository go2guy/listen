package com.interact.listen

import com.interact.listen.util.FileTypeDetector
import java.net.URI
import org.joda.time.DateTime
import org.joda.time.Duration

class Audio {
    DateTime dateCreated // auto-timestamped by GORM
    String description
    Duration duration
    File file
    DateTime lastUpdated // auto-timestamped by GORM
    String transcription = ''

    static constraints = {
        description nullable: true, blank: false, maxSize: 200
        transcription blank: true, maxSize: 4000
    }

    def afterDelete() {
        File mp3 = mp3File();
        if(!file.delete()) {
            log.error("Could not delete Audio [${file.absolutePath}] from disk")
        } else {
            log.debug("Audio file [${file.absolutePath}] successfully deleted from disk")
            if (mp3.exists()) {
                if (!mp3.delete()) {
                    log.error("Could not delete Audio [${mp3.absolutePath}] from disk")
                } else {
                    log.debug("Audio file [${file.absolutePath}] successfully deleted from disk")
                }
            }
        }
    }

    String detectContentType()
    {
        def detector = new FileTypeDetector()
        return detector.detectContentType(file)
    }

    File mp3File() {
        def uri = file.toURI().toString()
        uri = uri.replace('.wav', '.mp3')
        return new File(new URI(uri))
    }
}
