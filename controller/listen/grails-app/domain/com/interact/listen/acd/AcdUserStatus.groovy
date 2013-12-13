package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.acd.AcdQueueStatus
import org.joda.time.DateTime

class AcdUserStatus
{
    User user
    AcdQueueStatus acdQueueStatus
    DateTime statusModified
    boolean onACall

    static belongsTo = [user: User]
}
