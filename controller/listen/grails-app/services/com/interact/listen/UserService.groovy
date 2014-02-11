package com.interact.listen
import com.interact.listen.license.ListenFeature
import com.interact.listen.acd.*
import com.interact.listen.history.*
import org.apache.log4j.Logger

class UserService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def springSecurityService
    def licenseService

    User update(User user, def params, boolean byOperator = false)
    {
        def originalEmailAddress = user.emailAddress
        def originalPassword = user.password
        def originalRealName = user.realName
        def originalUsername = user.username

        log.debug "userService update params [${params}]"

        if(byOperator) {
            user.properties['username', 'pass', 'confirm', 'realName', 'emailAddress'] = params
        } else {
            user.properties['realName', 'emailAddress', 'pass', 'confirm'] = params
        }

        if(user.pass?.trim()?.length() > 0) {
            user.password = springSecurityService.encodePassword(user.pass)
        }
        if(user.validate() && user.save()) {
            cloudToDeviceService.sendContactSync()

            if (licenseService.canAccess(ListenFeature.ACD))
            {
/*
                log.debug "User has been saved, lets save skills [${params.skillIds}]"
                // We're going to make a list of our skills so we can work with it easier
                def skillIds = []
                if (params.skillIds.getClass() == String) {
                    skillIds << params.skillIds
                } else {
                    params.skillIds.each { id ->
                        skillIds << id
                    }
                }

                if(log.isDebugEnabled())
                {
                    log.debug "Params skill count [${skillIds}][${skillIds.size()}]"
                }
*/
                def organization = user.organization;
                def orgSkills = Skill.findAllByOrganization(organization);
                def existingUserSkills = UserSkill.findAllByUser(user);
                List<UserSkill> newSkills = new ArrayList<UserSkill>();

                for(Skill skill : orgSkills)
                {
                    int id = skill.id;
                    String selected = params.get("selected" + id);
                    String screenPriority = params.get("priority" + id);

                    if(selected && selected.equals("true"))
                    {
                        //did they have this before, did the priority change?
                        boolean existingSkill = false;

                        for(UserSkill userSkill : existingUserSkills)
                        {
                            if(userSkill.skill.id == skill.id)
                            {
                                //Yes they did, did the priority change

                                int newPriority = 0;
                                if(screenPriority)
                                {
                                    newPriority = Integer.parseInt(screenPriority);
                                }

                                if(newPriority != userSkill.priority)
                                {
                                    if(log.isDebugEnabled())
                                    {
                                        log.debug("Setting priority for skill[" + userSkill.skill.description +
                                                "] to [" + newPriority + "]");
                                    }
                                    userSkill.priority = newPriority;
                                    userSkill.save(flush: true);
                                }
                                existingSkill = true;
                                break;
                            }
                        }

                        if(!existingSkill)
                        {
                            UserSkill newSkill = new UserSkill();
                            newSkill.skill = skill;
                            newSkill.priority =
                                (screenPriority == null || screenPriority.isEmpty() ? 0 : Integer.parseInt(screenPriority));
                            newSkill.user = user;
                            newSkills.add(newSkill);
                        }
                    }
                    else
                    {
                        //Did they use to have this skill?
                        for(UserSkill userSkill : existingUserSkills)
                        {
                            if(userSkill.skill.id == skill.id)
                            {
                                //get rid of it
                                userSkill.delete(flush: true);
                                break;
                            }
                        }
                    }
                }

                //Add the new skills
                for(UserSkill newSkill : newSkills)
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug("Adding new skill: " + newSkill.skill.description);
                    }
                    newSkill.insert(flush: true);
                }

                // loop through existing user skills.  Remove skills that are no longer selected.
                // Remove skills that we already have from the skills list that we plan on adding


/*

                for(UserSkill existingUserSkill : existingUserSkills)
                {
                    if ( skillIds.contains(existingUserSkill.skill.id.toString()) )
                    {
                        if(log.isDebugEnabled())
                        {
                            log.debug "User [${user.username}] already has skill [${existingUserSkill.skill.skillname}]";
                        }

//                        if()
                        // we'll remove it from the skills list, since we already have it and don't need to add it to the db
                        skillIds.remove(existingUserSkill.skill.id.toString())
                    } else {
                        log.debug "User [${user.username}] already has skill [${existingUserSkill.skill.skillname}] and we need to delete it"
                        historyService.deletedUserSkill(existingUserSkill)
                        existingUserSkill.delete()
                    }
                }*/
                        
                // We should now be left with a list that has removed skills we already have, and we've deleted skills
                // from the db that are no longer selected
/*
                for(String skillId : skillIds)
                {
                    if(log.isDebugEnabled())
                    {
                        log.debug "Working to add skill [${skillId}]"
                    }
                    def userskill = new UserSkill()
                    userskill.skill = Skill.findById(skillId.toInteger())
                    if(log.isDebugEnabled())
                    {
                        log.debug "User [${user.username}] requires skill [${userskill.skill.skillname}]"
                    }
    
                    userskill.user = user
                    
                    if(userskill.validate() && userskill.save())
                    {
                        historyService.addedUserSkill(userskill)
                    }
                    else
                    {
                        log.error "Failed to add skill [${userskill.skill.skillname}] to user [${user.username}]"
                    }
                }
                */
            }


            if(originalEmailAddress != user.emailAddress) {
                historyService.changedAccountEmailAddress(user, originalEmailAddress)
                if(user.organization) {
                    ldapService.changeEmailAddress(user, originalEmailAddress, user.emailAddress)
                }
            }

            if(originalRealName != user.realName) {
                historyService.changedAccountName(user, originalRealName)
                if(user.organization) {
                    ldapService.changeName(user, user.realName)
                }
            }

            if(originalPassword != user.password) {
                historyService.changedAccountPassword(user)
            }

            if(originalUsername != user.username) {
                historyService.changedAccountUsername(user, originalUsername)
            }
        }
        return user
    }

    User disable(User user) {
        user.enabled = false
        if(user.validate() && user.save()) {
            historyService.disabledUser(user)
            // TODO disable on ldap?
        }
        return user
    }

    User enable(User user) {
        user.enabled = true
        if(user.validate() && user.save()) {
            historyService.enabledUser(user)
            // TODO enable on ldap?
        }
        return user
    }
}
