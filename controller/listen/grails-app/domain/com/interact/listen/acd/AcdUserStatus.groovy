package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.acd.AcdQueueStatus
import com.interact.listen.PhoneNumber
import org.joda.time.DateTime

class AcdUserStatus {
  User owner
  AcdQueueStatus acdQueueStatus
  DateTime statusModified
  boolean onACall
  PhoneNumber contactNumber

  static constraints = {
    statusModified nullable: true
    contactNumber nullable: true
  }

  /* static belongsTo = [user: User] */
}
