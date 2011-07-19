package com.interact.listen.fax

import com.interact.listen.User
import com.interact.listen.UserFile
import com.interact.listen.util.FileTypeDetector
import org.joda.time.DateTime

class OutgoingFax {
    DateTime dateCreated
    DateTime datePrepared
    DateTime dateSent
    String dnis
    File merged
    int pages = 0
    String preparationStatus = 'Preparing'
    User sender
    List toMerge = [] as List

    static hasMany = [toMerge: UserFile]

    static constraints = {
        datePrepared nullable: true
        dateSent nullable: true
        dnis blank: false
        merged nullable: true
        pages min: 0
        preparationStatus nullable: true, blank: true
        toMerge minSize: 1, validator: { val ->
            def detector = new FileTypeDetector()
            return val.every {
                detector.detectContentType(it.file) == 'application/pdf'
            }
        }
    }
}
