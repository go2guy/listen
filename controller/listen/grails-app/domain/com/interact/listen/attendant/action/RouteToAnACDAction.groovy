package com.interact.listen.attendant.action

import com.interact.listen.acd.AcdQueueStatus
import com.interact.listen.acd.UserSkill
import com.interact.listen.attendant.Menu
import com.interact.listen.acd.Skill
import com.interact.listen.utils.ListenPromptUtil

class RouteToAnACDAction extends Action
{
    def grailsApplication

    Skill skill
    
    static belongsTo = [ skill: Skill ]
    
    static constraints = {
        skill nullable: true
    }
    
    def toIvrCommand(String promptDirectory, String promptBefore, String artifactsDirectory, int organizationId)
    {
        def args = [
            applicationName: 'ACD',
            skillId: skill.id,
            skillname: skill.skillname,
            onHoldMsg: ListenPromptUtil.buildFileName(skill.onHoldMsg, ListenPromptUtil.ACD_LOCATION, organizationId),
            onHoldMsgExtended: ListenPromptUtil.buildFileName(skill.onHoldMsgExtended, ListenPromptUtil.ACD_LOCATION, organizationId),
            onHoldMusic: ListenPromptUtil.buildFileName(skill.onHoldMusic, ListenPromptUtil.ACD_LOCATION, organizationId),
            connectMsg: ListenPromptUtil.buildFileName(skill.connectMsg, ListenPromptUtil.ACD_LOCATION, organizationId)
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
