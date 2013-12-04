package com.interact.listen.acd

enum Status {
    AVAILABLE, UNAVAILABLE;

    String toString() {
        return name().substring(0,1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
