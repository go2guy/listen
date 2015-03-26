package com.interact.listen.attendant.action

class ReplayMenuAction extends Action
{
    // has no attributes

    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, long organizationId)
    {
        def args = [
        ]

        return [
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            action: 'REPLAY',
            args: args
        ]
    }
}
