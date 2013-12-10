package com.interact.listen

enum AssignablePermission {
    ATTENDANT      ('ROLE_ATTENDANT_ADMIN',    'Attendant',           false),
    CONFERENCING   ('ROLE_CONFERENCE_USER',    'Conferencing',        true),
    FAX            ('ROLE_FAX_USER',           'Fax',                 true),
    FIND_ME        ('ROLE_FINDME_USER',        'Find Me / Follow Me', true),
    VOICEMAIL      ('ROLE_VOICEMAIL_USER',     'Voicemail',           true),
    ADMINISTRATION ('ROLE_ORGANIZATION_ADMIN', 'Administration',      false),
    ACD            ('ROLE_ACD_USER',           'ACD',                 false)

    String authority
    String description
    boolean readOnly = false // temporary - ultimately we would like all to be "writable"

    private AssignablePermission(String authority, String description, boolean readOnly) {
        this.authority = authority
        this.description = description
        this.readOnly = readOnly
    }

    Role toRole() {
        Role.findByAuthority(authority)
    }
}
