package com.interact.listen.voicemail

enum PlaybackOrder {
    NEWEST_TO_OLDEST,
    OLDEST_TO_NEWEST;

    String getKey() { name() } // used for populating select list keys in the view

    String toString() {
        def name = name().toLowerCase().replaceAll(/_/, ' ')
        return name[0].toUpperCase() + name[1..-1]
    }
}
