package com.interact.listen
import com.interact.listen.acd.*
import com.interact.listen.history.*
import org.apache.log4j.Logger

class UserService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def springSecurityService

    User update(User user, def params, boolean byOperator = false) {
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
            
            log.debug "Params skill count [${skillIds}][${skillIds.size()}]"
            // loop through existing user skills.  Remove skills that are no longer selected.  Remove skills that we aleady have from the skills list that we plan on adding
            def existingUserSkills = UserSkill.findAllByUser(user)
            def skillCnt = UserSkill.countByUser(user)
            log.debug "Found [${skillCnt}] skills for this user"
            existingUserSkills.each { existingUserSkill ->
                if ( skillIds.contains(existingUserSkill.skill.id.toString()) ) {
                    log.debug "User [${user.username}] already has skill [${existingUserSkill.skill.skillname}] and we are keeping it"
                    // we'll remove it from the skills list, since we already have it and don't need to add it to the db
                    skillIds.remove(existingUserSkill.skill.id.toString())
                } else {
                    log.debug "User [${user.username}] already has skill [${existingUserSkill.skill.skillname}] and we need to delete it"
                    historyService.deletedUserSkill(existingUserSkill)
                    existingUserSkill.delete()
                }
            }
                    
            // We should now be left with a list that has removed skills we already have, and we've deleted skills from the db that are no longer selected   
            skillIds.each { skillId ->
                
                log.debug "Working to add skill [${skillId}]"
                def userskill = new UserSkill()
                userskill.skill = Skill.findById(skillId.toInteger())
                log.debug "User [${user.username}] requires skill [${userskill.skill.skillname}]"

                userskill.user = user
                
                if(userskill.validate() && userskill.save()) {
                    historyService.addedUserSkill(userskill)
                } else {
                    log.error "Failed to add skill [${userskill.skill.skillname}] to user [${user.username}]"
                }
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
