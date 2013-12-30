package com.interact.listen.acd

import com.interact.listen.User
import org.joda.time.DateTime

class AcdCallHistory
{
    static constraints =
    {
        ivr nullable: true
        enqueueTime nullable: true
        lastModified nullable: true
        autoTimestamp: true
        dequeueTime nullable: true
        userId nullable: true
        callStart nullable: true
        callEnd nullable: true
    }

    String ani;
    String dnis;
    long skillId;
    String sessionId;
    DateTime enqueueTime;
    AcdCallStatus callStatus;
    String ivr;
    long userId;
    DateTime lastModified;
    DateTime dequeueTime;
    DateTime callStart;
    DateTime callEnd;

    public AcdCallHistory(AcdCall callRecord)
    {
        this.ani = callRecord.ani;
        this.dnis = callRecord.dnis;
        this.skillId = callRecord.skill.id;
        this.sessionId = callRecord.sessionId;
        this.enqueueTime = callRecord.enqueueTime;
        this.callStatus = callRecord.callStatus;
        this.ivr = callRecord.ivr;

        if(callRecord.user != null)
        {
            this.userId = callRecord.user.id;
        }
        this.lastModified = callRecord.lastModified;
        this.callStart = callRecord.callStart;
        this.callEnd = callRecord.callEnd;
    }

    /**
     * Executed prior to an insert.
     */
    def beforeInsert()
    {
        this.setDequeueTime(new DateTime());
    }

}
