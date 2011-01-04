package com.interact.listen.android.voicemail.sync;

enum SyncType
{
    INITIALIZE,   // ContentResolver.SYNC_EXTRAS_INITIALIZE
    PERIODIC,     // Periodic check for check that there aren't new voice mails
    UPLOAD_ONLY,  // Only push updates
    USER_SYNC,    // User clicked refresh or cleared cache
    CONFIG_SYNC,  // Change in cloud configuration
    CLOUD_SYNC,   // Otherwise, coming from the C2DM
    LEGACY,       // an old sync that needs to be removed
}
