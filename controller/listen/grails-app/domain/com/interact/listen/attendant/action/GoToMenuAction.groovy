package com.interact.listen.attendant.action

import com.interact.listen.attendant.Menu

class GoToMenuAction extends Action
{

    // TODO this has potential for bugs, since we arent referencing
    // the destination menu directly. however, for now, it makes it
    // easier to use GORM to cascade everything down and not have a
    // circular relationship back up to Menu. blech.

    String destinationMenuGroupName
    String destinationMenuName

    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, long organizationId)
    {
        //Not implemented.
        return null;
    }
}
