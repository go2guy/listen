package com.interact.listen

import com.interact.listen.util.FileTypeDetector
import com.interact.listen.util.FileTypeDetectionException

class UserFile {
    String detectedType = 'application/octet-stream'
    File file
    User owner

    static constraints = {
        detectedType blank: false
    }

    def beforeInsert() {
        try {
            def detector = new FileTypeDetector()
            detectedType = detector.detectContentType(file)
        } catch(FileTypeDetectionException e) {
            log.error(e)
        }
    }

    def afterDelete() {
        if(!file.delete()) {
            log.error("Could not delete UserFile [${file.absolutePath}] from disk")
        }
    }

    static File pathFor(User user, String subdir, String filename) {
        if(!user?.id) {
            throw new IllegalArgumentException('Cannot get UserFile path for null user id')
        }
        File dir = new File("/interact/listen/artifacts/${user.id}/files")
        if(subdir) {
            dir = new File(dir, subdir)
        }

        if(!dir.exists()) {
            dir.mkdirs()
        }

        new File(dir, filename)
    }

    static File pathFor(User user, String filename) {
        pathFor(user, null, filename)
    }
}
