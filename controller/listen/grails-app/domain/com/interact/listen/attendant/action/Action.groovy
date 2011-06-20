package com.interact.listen.attendant.action

import com.interact.listen.attendant.Menu

class Action { // TODO make this class abstract after this is fixed - http://jira.grails.org/browse/GRAILS-6780
    String keysPressed
    String promptBefore // path to prompt wav file to play before taking action

    static belongsTo = Menu

    static constraints = {
        // FIXME nullable since default and timeout actions dont have keypresses, need to rearchitect
        keysPressed nullable: true, blank: false, matches: '^[0-9\\?\\*#]+$' // question mark is a single-character wildcard
    }
}
