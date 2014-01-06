package com.interact.listen.attendant.action

import com.interact.listen.acd.AcdQueueStatus
import com.interact.listen.acd.UserSkill
import com.interact.listen.attendant.Menu
import com.interact.listen.acd.Skill

class RouteToAnACDAction extends Action {
    Skill skill
    
    static belongsTo = [ skill: Skill ]
    
    static constraints = {
        skill nullable: true
    }
    
    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory)
    {
        def args = [
            applicationName: 'ACD',
            skillId: skill.id,
            skillname: skill.skillname,
            onHoldMsg: artifactsDirectory + '/acd/' + skill.id + '/onHoldMsg.wav',
            onHoldMsgExtended: artifactsDirectory + '/acd/' + skill.id  + '/onHoldMsgExtended.wav',
            onHoldMusic: artifactsDirectory + '/acd/' + skill.id  + '/onHoldMusic.wav',
            connectMsg: artifactsDirectory + '/acd/' + skill.id  + '/connectMsg.wav'
        ]

        //Determine voicemail extension for the selected skill
        boolean voicemailFound = false;

        Set<UserSkill> skills = skill.userSkill;
        for(UserSkill thisSkill : skills)
        {
            if(thisSkill.user.acdUserStatus.AcdQueueStatus == AcdQueueStatus.VoicemailBox )
            {
                if(thisSkill.user.acdUserStatus.contactNumber != null)
                {
                    String extension = thisSkill.user.acdUserStatus.contactNumber.number;
                    args.put("voicemailExt", extension);
                    voicemailFound = true;
                    break;
                }
            }
        }

        if(!voicemailFound)
        {
            log.warn("Unable to locate an ACD Voicemail Box for skill: " + skill.description);
        }

        return [
            promptBefore: !promptBefore || promptBefore.trim() == '' ? '' : promptDirectory + '/' + promptBefore,
            action: 'LAUNCH_APPLICATION',
            args: args
        ]
    }
}
