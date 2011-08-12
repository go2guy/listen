package com.interact.listen

enum AssignablePermission {
    ATTENDANT('ROLE_ATTENDANT_ADMIN', 'Attendant'),
    CONFERENCING('ROLE_CONFERENCE_USER', 'Conferencing'),
    FAX('ROLE_FAX_USER', 'Fax'),
    FIND_ME('ROLE_FINDME_USER', 'Find Me / Follow Me'),
    VOICEMAIL('ROLE_VOICEMAIL_USER', 'Voicemail'),
    ADMINISTRATION('ROLE_ORGANIZATION_ADMIN', 'Administration')

    String authority
    String description

    private AssignablePermission(String authority, String description) {
        this.authority = authority
        this.description = description
    }

    Role toRole() {
        Role.findByAuthority(authority)
    }
}
