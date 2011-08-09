package com.interact.listen.fax

import com.interact.listen.User
import com.interact.listen.UserFile
import com.interact.listen.util.FileTypeDetector
import org.joda.time.DateTime

class OutgoingFax {
    DateTime dateCreated
    DateTime dateSent
    String dnis
    int pages = 0
    User sender
    List sourceFiles = [] as List

    static hasMany = [sourceFiles: UserFile]

    static constraints = {
        dateSent nullable: true
        dnis blank: false
        pages min: 0
        sourceFiles minSize: 1, validator: { val ->
            def detector = new FileTypeDetector()
            return val.every {
                detector.detectContentType(it.file) == 'application/pdf'
            }
        }
    }
}
