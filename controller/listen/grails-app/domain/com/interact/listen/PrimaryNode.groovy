package com.interact.listen

import java.sql.Timestamp

class PrimaryNode {

    String nodeName
    Timestamp last_modified

    static constraints = {
        nodeName nullable: true
    }
}
