package com.interact.listen.history

enum Service {
    APPLICATION,
    CONFERENCING,
    CONFIGURATION,
    VOICEMAIL;

    String toString()
    {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
