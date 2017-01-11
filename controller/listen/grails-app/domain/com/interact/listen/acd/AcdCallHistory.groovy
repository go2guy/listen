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
        user nullable: true
        agentCallStart nullable: true
        agentCallEnd nullable: true
        agentNumber nullable: true
    }

    String ani;
    String dnis;
    Skill skill;
    String sessionId;
    DateTime enqueueTime;
    AcdCallStatus callStatus;
    String ivr;
    User user;
    String agentNumber;
    DateTime lastModified;
    DateTime dequeueTime;
    DateTime agentCallStart;
    DateTime agentCallEnd;

    static belongsTo = [user: User, skill: Skill ]

    public AcdCallHistory(AcdCall callRecord)
    {
        this.ani = callRecord.ani;
        this.dnis = callRecord.dnis;
        this.skill = callRecord.skill;
        this.sessionId = callRecord.sessionId;
        this.enqueueTime = callRecord.enqueueTime;
        this.dequeueTime = callRecord.callStart;
        this.callStatus = callRecord.callStatus;
        this.ivr = callRecord.ivr;
        if ( callRecord.user != null ) {
            this.user = callRecord.user;
            this.agentNumber = AcdUserStatus.findByOwner(this.user)?.contactNumber?.number
        }
        this.lastModified = callRecord.lastModified;
        this.agentCallStart = callRecord.callStart;
        this.agentCallEnd = callRecord.callEnd;
    }

    /**
     * Executed prior to an insert.
     */
    //def beforeInsert()
    //{
    //    this.setDequeueTime(new DateTime());
    //}

    static String csvHeader()
    {
        StringBuffer returnVal = new StringBuffer();
        returnVal.append("skill,");
        returnVal.append("enqueueTime,");
        returnVal.append("dequeueTime,");
        returnVal.append("queueTime,");
        returnVal.append("callStatus,");
        returnVal.append("agent,")
        returnVal.append("agentCallStart,");
        returnVal.append("agentCallEnd,");
        returnVal.append("agentTime,");
        return returnVal;
    }
}
