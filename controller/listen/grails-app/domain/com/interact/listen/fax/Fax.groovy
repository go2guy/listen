package com.interact.listen.fax

import com.interact.listen.InboxMessage

// incoming fax message
class Fax extends InboxMessage {
    String ani
    File file

    static constraints = {
        // all properties must be nullable (inheritance)
        ani nullable: true, blank: false
        file nullable: true
    }

    def afterDelete() {
        if(!file?.delete()) {
            log.error("Could not delete UserFile [${file.absolutePath}] from disk")
        }
    }
}