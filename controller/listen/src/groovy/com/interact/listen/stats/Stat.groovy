package com.interact.listen.stats

enum Stat {
    // General
    CONTROLLER_STARTUP                    ('2012'),
    GUI_LOGIN                             ('1004'),
    GUI_LOGOUT                            ('1005'),

    // Conferencing
    CONFERENCE_ADMIN_JOIN                 ('2001'),
    CONFERENCE_ACTIVE_JOIN                ('2002'),
    CONFERENCE_PASSIVE_JOIN               ('2003'),
    CONFERENCE_START                      ('2004'),
    CONFERENCE_LENGTH                     ('2005'),
    CONFERENCE_RECORDING_START            ('2006'),
    CONFERENCE_RECORDING_STOP             ('2007'),

    // Email and SMS
    NEW_VOICEMAIL_EMAIL                   ('2008'),
    NEW_VOICEMAIL_SMS                     ('2009'),
    NEW_VOICEMAIL_SMS_ALTERNATE           ('2013'),
    NEW_VOICEMAIL_RECURRING_SMS           ('2010'),
    NEW_VOICEMAIL_RECURRING_SMS_ALTERNATE ('2011'),
    TEST_VOICEMAIL_SMS                    ('2016'),
    TEST_VOICEMAIL_EMAIL                  ('2017'),
    CONFERENCE_INVITE_EMAIL               ('2014'),
    CONFERENCE_CANCEL_EMAIL               ('2015'), // TODO still needs to be written

    // Listen -> SPOT communication
    SPOT_AUTO_DIAL_DIAL                   ('5000'),
    SPOT_CONF_EVENT_BRIDGE_DIAL           ('5001'),
    SPOT_CONF_EVENT_UNMUTE                ('5002'),
    SPOT_CONF_EVENT_MUTE                  ('5003'),
    SPOT_CONF_EVENT_DROP                  ('5004'),
    SPOT_DEL_ARTIFACT_FILE                ('5005'),
    SPOT_DEL_ARTIFACT_SUB                 ('5006'),
    SPOT_RECORD_START                     ('5007'),
    SPOT_RECORD_STOP                      ('5008'),
    SPOT_MSG_LIGHT_ON                     ('5009'),
    SPOT_MSG_LIGHT_OFF                    ('5010'),

    // C2DM (Android)
    C2DM_DISCARD_DUE_TO_RETRYS            ('3100'),
    C2DM_QUOTA_EXCEEDED                   ('3101'),
    C2DM_DEVICE_QUOTA_EXCEEDED            ('3102'),
    C2DM_SERVICE_UNAVAILABLE              ('3103'),
    C2DM_INVALID_REGISTRATION             ('3104'),
    C2DM_NOT_REGISTERED                   ('3105'),
    C2DM_INVALID_AUTH_TOKEN               ('3106'),
    C2DM_UNKNOWN_ERROR                    ('3107'),
    C2DM_QUEUED_MESSAGE                   ('3110'),
    C2DM_QUEUED_RETRY                     ('3111'),
    C2DM_SENT_SUCCESFULLY                 ('3112'),
    C2DM_REGISTERED_DEVICE                ('3113'),
    C2DM_UNREGISTERED_DEVICE              ('3114')

    final String id

    Stat(id) {
        this.id = id
    }

    String value() {
        "LSTNCTL_${id}"
    }
}
