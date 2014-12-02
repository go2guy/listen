package com.interact.listen.attendant.action

import com.interact.listen.attendant.Menu

abstract class Action
{
    String keysPressed
    String promptBefore // path to prompt wav file to play before taking action

    static belongsTo = Menu

    static constraints =
    {
        // FIXME nullable since default and timeout actions dont have keypresses, need to rearchitect
        keysPressed nullable: true, blank: false, matches: '^[0-9\\?\\*#]+$' // question mark is a single-character wildcard
    }

    abstract toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, int organizationId);
}
