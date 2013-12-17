package com.interact.listen.attendant.action

import com.interact.listen.attendant.Menu
import com.interact.listen.acd.Skill

class RouteToAnACDAction extends Action {
    Skill skill
    
    static belongsTo = [ skill: Skill ]
    
    static constraints = {
        skill nullable: true
    }
    
    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory) {
        def args = [
            skillId: skill.id,
            skillname: skill.skillname,
            onHoldMsg: artifactsDirectory + '/acd/' + skill.id + '/onHoldMsg.wav',
            onHoldMsgExtended: artifactsDirectory + '/acd/' + skill.id  + '/onHoldMsgExtended.wav',
            onHoldMusic: artifactsDirectory + '/acd/' + skill.id  + '/onHoldMusic.wav',
            connectMsg: artifactsDirectory + '/acd/' + skill.id  + '/connectMsg.wav'
        ]

        return [
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            action: 'ROUTE_TO_AN_ACD',
            args: args
        ]
    }
}
