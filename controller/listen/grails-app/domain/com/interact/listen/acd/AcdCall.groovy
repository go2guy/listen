package com.interact.listen.acd

import com.interact.listen.User
import org.joda.time.DateTime

class AcdCall
{
    static constraints =
    {
        ivr nullable: true
        user nullable: true
        autoTimestamp: true
    }

    String ani;
    String dnis;
    Skill skill;
    String sessionId;
    DateTime enqueueTime;
    AcdCallStatus callStatus;
    String ivr;
    User user;
}

enum AcdCallStatus
{
    WAITING,
    CONNECT_REQUESTED,
    CONNECTED,
    COMPLETED
}
