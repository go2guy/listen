package com.interact.listen.attendant.action

class EndCallAction extends Action
{
    // has no attributes
    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, int organizationId)
    {
        return [
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            action: 'END_CALL'
        ]
    }
}
