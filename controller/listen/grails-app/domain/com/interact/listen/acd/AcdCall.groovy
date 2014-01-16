package com.interact.listen.acd

import com.interact.listen.User
import org.joda.time.DateTime

/**
 * Domain class for an entry in the acd_call table.
 */
class AcdCall
{
    static constraints =
    {
        ivr nullable: true
        user nullable: true
        enqueueTime nullable: true
        lastModified nullable: true
        autoTimestamp: true
        callStart nullable: true
        callEnd nullable: true
    }

    String ani;
    String dnis;
    Skill skill;
    String sessionId;
    DateTime enqueueTime;
    AcdCallStatus callStatus;
    String ivr;
    User user;
    DateTime callStart;
    DateTime callEnd;
    DateTime lastModified;
    boolean onHold = false;

    /**
     * Executed prior to an update.
     */
    def beforeUpdate()
    {
        this.setLastModified(DateTime.now());
    }

    /**
     * Executed prior to an insert.
     */
    def beforeInsert()
    {
        this.setEnqueueTime(new DateTime());
        this.setLastModified(DateTime.now());
    }
}

/**
 * Available values for the status of an acd call.
 */
enum AcdCallStatus
{
    WAITING,
    CONNECT_REQUESTED,
    CONNECTED,
    COMPLETED,
    CONNECT_FAIL,
    DISCONNECTED,
    VOICEMAIL

    public String viewable()
    {
        StringBuilder sb = new StringBuilder(this.toString().toLowerCase());
        char firstLetter = sb.charAt(0);
        sb.setCharAt(0, firstLetter.toUpperCase());
        return sb.toString();
    }
}
