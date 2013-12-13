package com.interact.listen.attendant

import com.interact.listen.attendant.action.*
import com.interact.listen.acd.*
import com.interact.listen.voicemail.TimeRestriction
import org.joda.time.LocalTime
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import org.apache.log4j.Logger
import org.springframework.context.MessageSource

class MenuGroupService {

    def springSecurityService
    MessageSource messageSource

    def saveGroups(def jsonGroups) {
        
        def user = springSecurityService.getCurrentUser()
        def missingGroups = MenuGroup.findAllByOrganization(user.organization) as Set

        def groups = []
        jsonGroups.each { jsonGroup ->
            def group
            if(jsonGroup.containsKey('id')) {
                group = MenuGroup.get(jsonGroup.get('id'))
                if(group) {
                    missingGroups.remove(group)
                }
            }
                
            if(!group) {
                group = new MenuGroup()
            }

            if(group.id) {
                group.menus?.clear()
                group.restrictions?.clear()
                Menu.findAllByMenuGroup(group)*.delete(flush: true)
            }


            group.name = (String)jsonGroup.get('name')
            group.isDefault = (Boolean)jsonGroup.get('isDefault')
            group.organization = user.organization

            def jsonRestrictions = (JSONArray)jsonGroup.get('restrictions')
            jsonRestrictions.each { jsonRestriction ->
                def startTime = (JSONObject)jsonRestriction.get('startTime')
                def endTime = (JSONObject)jsonRestriction.get('endTime')

                def restriction = new TimeRestriction()
                restriction.startTime = new LocalTime(startTime.get('h') as int, startTime.get('m') as int)
                restriction.endTime = new LocalTime(endTime.get('h') as int, endTime.get('m') as int)
                restriction.monday = jsonRestriction.get('monday')
                restriction.tuesday = jsonRestriction.get('tuesday')
                restriction.wednesday = jsonRestriction.get('wednesday')
                restriction.thursday = jsonRestriction.get('thursday')
                restriction.friday = jsonRestriction.get('friday')
                restriction.saturday = jsonRestriction.get('saturday')
                restriction.sunday = jsonRestriction.get('sunday')
                group.addToRestrictions(restriction)
            }

            def jsonMenus = (JSONArray)jsonGroup.get('menus')
            jsonMenus.each { jsonMenu ->
                def label = (String)jsonMenu.get('label')
                def isEntry = (Boolean)jsonMenu.get('entryMenu')
                def optionsPrompt = (String)jsonMenu.get('optionsPrompt')

                def menu = new Menu(name: label,
                                    isEntry: isEntry,
                                    optionsPrompt: optionsPrompt)

                // default action
                def jsonDefaultAction = jsonMenu.get('defaultAction')
                def defaultAction = keyToAction((String)jsonDefaultAction.get('directive'))
                populateAction(jsonDefaultAction, group.name, defaultAction)
                menu.defaultAction = defaultAction

                // timeout action
                def jsonTimeoutAction = jsonMenu.get('timeoutAction')
                def timeoutAction = keyToAction((String)jsonTimeoutAction.get('directive'))
                populateAction(jsonTimeoutAction, group.name, timeoutAction)
                menu.timeoutAction = timeoutAction

                def jsonActions = jsonMenu.get('actions') as List

                // keypress actions
                jsonActions.each { jsonAction ->
                    def directive = (String)jsonAction.get('directive')
                    def action = keyToAction(directive)
                    populateAction(jsonAction, group.name, action)
                    menu.addToKeypressActions(action)
                }

                group.addToMenus(menu)
            }

            // cascades to all associations
            groups.push(group)
        }

        missingGroups.each {
            log.debug "Deleting group [${it.name}]"
            it.delete()
        }

        groups.each { group ->
            if(!(group.validate() && group.save())) {
                log.debug "Group named ${group.name} didn't validate"

                // triggers transaction rollback
                throw new MenuGroupValidationException("Group named [${group.name}] failed validation", group.errors, groups)
            }
        }

        return true
    }

    private Action keyToAction(String key)
    {
        log.debug "We've received action [${key}]"
        
        if(key == "${messageSource.getMessage('attendant.menugroup.goToAMenu', null, null)}")
        {
            log.debug "Action goToAMenu"
            return new GoToMenuAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.dialANumber', null, null)}")
        {
            log.debug "Action dialANumber"
            return new DialNumberAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.dialWhatTheyPressed', null, null)}")
        {
            log.debug "Action dialWhatTheyPressed"
            return new DialPressedNumberAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.launchAnApplication', null, null)}")
        {
            log.debug "Action launchAnApplication"
            return new LaunchApplicationAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.replayThisMenu', null, null)}")
        {
            log.debug "Action replayMenuAction"
            return new ReplayMenuAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.routeToAnACD', null, null)}")
        {
            log.debug "Action routeToAnACDAction"
            return new RouteToAnACDAction()
        }
        else if(key == "${messageSource.getMessage('attendant.menugroup.endTheCall', null, null)}")
        {
            log.debug "Action endTheCall"
            return new EndCallAction()
        }

        log.error "We've received an action we can't handled [${key}]"
        throw new IllegalArgumentException("Cannot create Action from unknown key [${key}]")
    }

    private void populateAction(JSONObject json, def groupName, Action action)
    {
        def keypress = (String)json.get("keypress")
        def promptBefore = (String)json.get("promptBefore")
        def directive = (String)json.get("directive")
        def argument = (String)json.get("argument")

        if(action?.instanceOf(GoToMenuAction))
        {
            action.destinationMenuGroupName = groupName
            action.destinationMenuName = argument
        }
        else if(action?.instanceOf(DialNumberAction))
        {
            action.number = argument
        }
        else if(action?.instanceOf(LaunchApplicationAction))
        {
            action.applicationName = argument
        }
        else if(action?.instanceOf(RouteToAnACDAction))
        {
            log.debug "We've selected ACD Action [${argument}][${action}]"
            def skill = Skill.findBySkillname(argument)
            if (skill) {
                log.debug "We've found the appropriate skill [${skill.skillname}]"
                action.skill = skill
            } else {
                log.error "We didn't find skill for skillname [${argument}]"
            }
        }
        if(action == null)
        {
            throw new AssertionError("Action [${directive}] for keypress [${keypress}] is not a valid action")
        }

        action.promptBefore = promptBefore
        action.keysPressed = keypress
    }
}
