package com.interact.listen.acd

class Status {
  String name
  String description

  static contraints = {
    name unique: true
  }
}
