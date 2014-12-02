package com.interact.listen.attendant.action

class LaunchApplicationAction extends Action
{
    String applicationName

    static constraints = {
        applicationName nullable: true, blank: false, validator: { val -> return (val == null ? 'nullable' : true) }
    }

    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, long organizationId)
    {
        def args = [
            applicationName: applicationName
        ]

        return [
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            action: 'LAUNCH_APPLICATION',
            args: args
        ]
    }
}
