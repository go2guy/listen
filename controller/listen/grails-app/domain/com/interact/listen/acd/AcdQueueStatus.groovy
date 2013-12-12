package com.interact.listen.acd

class AcdQueueStatus {
  String name
  String description

  static contraints = {
    name unique: true
  }
}
