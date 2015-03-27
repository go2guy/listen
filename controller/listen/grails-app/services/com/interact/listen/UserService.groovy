package com.interact.listen
import com.interact.listen.httpclient.HttpClientImpl
import com.interact.listen.license.ListenFeature
import com.interact.listen.acd.*
import com.interact.listen.history.*
import org.apache.log4j.Logger
import java.sql.*
import java.io.*
import groovy.sql.Sql

class UserService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def springSecurityService
    def licenseService
    def dataSource

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
                                def origPriority = userSkill.priority;
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
                                    historyService.updatePriorityUserSkill(userSkill, origPriority);
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
                                historyService.deletedUserSkill(userSkill)
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
                    historyService.addedUserSkill(newSkill);
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
    
    // AJB 11/24/2014 - Here is where we delete the user.
    def deleteUser(User user) {
        // set the user id to null in action_history
        def db = new Sql(dataSource)
        def sql_str
        sql_str =  "update listen2.action_history set by_user_id = null where by_user_id = ${user.id}"
        db.executeUpdate(sql_str)
        sql_str =  "update listen2.action_history set on_user_id = null where on_user_id = ${user.id}"
        db.executeUpdate(sql_str)
        
        // set the user id to null in call_history
        sql_str =  "update listen2.call_history set from_user_id = null where from_user_id = ${user.id}"
        db.executeUpdate(sql_str)
        sql_str =  "update listen2.call_history set to_user_id = null where to_user_id = ${user.id}"
        db.executeUpdate(sql_str)

        // set the user id to null in inbox_message
        sql_str =  "update listen2.inbox_message set forwarded_by_id = null where forwarded_by_id = ${user.id}"
        db.executeUpdate(sql_str)
        sql_str =  "update listen2.inbox_message set left_by_id = null where left_by_id = ${user.id}"
        db.executeUpdate(sql_str)
        
        user.delete()
        return true        
    }
    
    // AJB 11/24/2014 - Here is where we clear the audio table and the physical files on the hard drive.
    def cleanUpAudio(User user) {
        def grailsApplication
        
        // set the user id to null in action_history
        def db = new Sql(dataSource)
        def sql_str
        
        // Now delete audio files.  What we are going to do is search for the user id in the path of the audio file.
        // This is easier since it's one search. Otherwise we would have to do a combinded query of inbox_message, participant, phone_number & recording
        // to get all the IDs of the audio files.
        //sql_str =  "delete from audio where file like '%/${user.id}/%'"
        StringBuffer sb = new StringBuffer("delete from audio where file like '%/")
        sb.append(user.id)
        sb.append("/%'" )
        sql_str = sb.toString()
        log.debug "User STRING : ${sql_str}"
        db.executeUpdate(sql_str)
        
        // Let's delete the physical files. Then we are done.
        //StringBuffer sb2 = new StringBuffer(grailsApplication.config.com.interact.listen.artifactsDirectory)
        StringBuffer sb2 = new StringBuffer('/interact/listen/artifacts/')
        sb2.append(user.id)
        log.debug(sb2.toString())
        //def directory = new File(sb2.toString())
        //if (directory.deleteDir())
        //    log.debug "Directory Deleted"
        //else
        //    log.debug "Directory NOT Deleted"
            
        def url = "http://localhost/spot/cgi-bin/spotbuild/fileops.php"
        def httpClient = new HttpClientImpl()
        httpClient.setSocketTimeout(3000)
        def params = [
            "FILE1" : sb2.toString(),
            "OPERATION" : "delete"
        ]
        httpClient.post(url, params)
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
