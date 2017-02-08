package com.interact.listen.history

import com.interact.listen.acd.AcdCallHistory
import com.interact.listen.Organization
import com.interact.listen.User
import org.joda.time.DateTime
import org.joda.time.Duration

class CallHistory {
    DateTime dateTime
    String ani
    String outboundAni
    String dnis
    String inboundDnis
    Duration duration
    Organization organization
    User fromUser
    User toUser
    String sessionId
    String commonCallId
    String ivr
    String result
    DateTime lastModified;
    int cdrPostResult
    int cdrPostCount

    Boolean acdCall

    static transients = [ 'acdCall' ]

    static constraints = {
        fromUser nullable: true
        toUser nullable: true
        lastModified nullable: true
        acdCall nullable: true
        // cdrPostResult nullable: false // not supported because int
        // cdrPostCount nullable: false // not supported because int
    }
    static mapping = {
        dateTime column: "date_time", sqlType: "DATETIME(3)", precision: 3
    }
    /**
     * Executed prior to an insert.
     */
    def beforeInsert()
    {
        this.setLastModified(new DateTime());
    }

    static String csvHeader()
    {
        StringBuffer returnVal = new StringBuffer();
        returnVal.append("began,");
        returnVal.append("calling party,");
        returnVal.append("called party,");
        returnVal.append("duration,")
        returnVal.append("call result,");
        return returnVal;
    }

    // This method works, but we're unable to access our taglib formatting libraries from this domain object
    String csvRow()
    {
        StringBuffer returnVal = new StringBuffer();
        returnVal.append(this?.dateTime?.toString("yyyy-MM-dd HH:mm:ss"))?.append(",");
        returnVal.append(this?.ani)?.append(",");
        returnVal.append(this?.dnis)?.append(",");
        returnVal.append(this.duration)?.append(",");
        returnVal.append(this?.result.replaceAll(","," "))?.append(",");
        return returnVal;
    }
}
