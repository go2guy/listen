package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.history.CallHistory
import org.joda.time.DateTime

class AcdHistory
{
    CallHistory callHistory;
    Skill skill;
    DateTime enqueueTime;
    DateTime dequeueTime;
    AcdCallStatus callStatus;
    User user;
    DateTime agentCallStart;
    DateTime agentCallEnd;
    DateTime lastModified;

    static belongsTo = [ callHistory: CallHistory, user: User, skill: Skill ]

    static constraints =
    {
        enqueueTime nullable: true
        lastModified nullable: true
        autoTimestamp: true
        dequeueTime nullable: true
        user nullable: true
        agentCallStart nullable: true
        agentCallEnd nullable: true
    }

    public AcdHistory(AcdCall callRecord)
    {
        this.skill = callRecord.skill;
        this.enqueueTime = callRecord.enqueueTime;
        this.callStatus = callRecord.callStatus;
        if ( callRecord.user != null ) {
            this.user = callRecord.user;
        }
        this.lastModified = callRecord.lastModified;
        this.agentCallStart = callRecord.callStart;
        this.agentCallEnd = callRecord.callEnd;
    }

    /**
     * Executed prior to an insert.
     */
    def beforeInsert()
    {
        this.setDequeueTime(new DateTime());
    }

    static String csvHeader()
    {
        StringBuffer returnVal = new StringBuffer();
        returnVal.append("skill,");
        returnVal.append("enqueueTime,");
        returnVal.append("callStatus,");
        returnVal.append("agent,")
        returnVal.append("dequeueTime,");
        returnVal.append("agentCallStart,");
        returnVal.append("agentCallEnd,");
        return returnVal;
    }

    String csvRow()
    {
        StringBuffer returnVal = new StringBuffer();
        returnVal.append(this?.skill?.skillname)?.append(",");
        returnVal.append(this?.enqueueTime?.toString("yyyy-MM-dd HH:mm:ss"))?.append(",");
        returnVal.append(this?.callStatus?.viewable())?.append(",");
        returnVal.append(this?.user?.username)?.append(",");
        returnVal.append(this?.dequeueTime?.toString("yyyy-MM-dd HH:mm:ss"))?.append(",");
        returnVal.append(this?.agentCallStart?.toString("yyyy-MM-dd HH:mm:ss"))?.append(",");
        returnVal.append(this?.agentCallEnd?.toString("yyyy-MM-dd HH:mm:ss"))?.append(",");
        return returnVal;
    }
}
