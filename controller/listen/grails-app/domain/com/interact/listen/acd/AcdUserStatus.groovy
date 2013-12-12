package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.acd.AcdQueueStatus

class AcdUserStatus {
  User user
  AcdQueueStatus acdQueueStatus

  static belongsTo = [user: User]
}
