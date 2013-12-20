package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.PhoneNumber
import org.joda.time.DateTime

class AcdUserStatus {
  User owner
  AcdQueueStatus AcdQueueStatus
  DateTime statusModified
  boolean onACall
  PhoneNumber contactNumber

  static constraints = {
    statusModified nullable: true
    contactNumber nullable: true
  }
}

enum AcdQueueStatus {
  AVAILABLE("Available"),
  UNAVAILABLE("Unavailable")

  final String value

  AcdQueueStatus(String value) { this.value = value }

  static AcdQueueStatus fromString(String value) {
    return this.valueOf(value.toUpperCase())
  }

  String toString() { value }
  String getKey() { name() }
}
