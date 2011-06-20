package com.interact.listen.attendant

import com.interact.listen.attendant.action.*
import com.interact.listen.voicemail.TimeRestriction
import grails.plugins.springsecurity.Secured
import org.joda.time.LocalTime
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.JSONValue

@Secured(['ROLE_ATTENDANT_ADMIN'])
class AttendantController {
    static allowedMethods = [
        index: 'GET',
        menu: 'GET'
    ]

    def promptFileService // injected
    def springSecurityService // injected

    def index = {
        redirect(action: 'menu')
    }

    def menu = {
        def user = springSecurityService.getCurrentUser()
        def groups = MenuGroup.findAllByOrganization(user.organization, [sort: 'isDefault', order: 'desc'])
        render(view: 'menu', model: [groups: groups])
    }

    def save = {
        Menu.withTransaction { status ->
            JSONArray jsonGroups = (JSONArray)JSONValue.parse(params.groups)
            log.debug "Received menu for saving: ${jsonGroups.toJSONString()}"

            def user = springSecurityService.getCurrentUser()
            MenuGroup.findAllByOrganization(user.organization).each {
                it.delete()
            }

            def groups = []
            jsonGroups.each { jsonGroup ->
                def group = new MenuGroup()
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

            boolean valid = true
            groups.each { group ->
                if(!(group.validate() && group.save())) {
                    log.debug "Group named ${group.name} didn't validate"
                    valid = false
                    status.setRollbackOnly()
                }
            }

            if(!valid) {
                render(view: 'menu', model: [groups: groups])
            } else {
                flash.successMessage = 'Menu saved'
                redirect(action: 'menu')
            }
        }
    }

    def uploadPrompt = {
        def file = request.getFile('uploadFile')
        if(!file) {
            render('Please select a file to upload')
            return
        }

        def user = springSecurityService.getCurrentUser()
        promptFileService.save(file, user.organization.id)

        render('Success')
    }

    private Action keyToAction(String key)
    {
        if(key.equals("Go To A Menu..."))
        {
            return new GoToMenuAction()
        }
        else if(key.equals("Dial A Number..."))
        {
            return new DialNumberAction()
        }
        else if(key.equals("Dial What They Pressed"))
        {
            return new DialPressedNumberAction()
        }
        else if(key.equals("Launch An Application..."))
        {
            return new LaunchApplicationAction()
        }
        else if(key.equals("Replay This Menu"))
        {
            return new ReplayMenuAction()
        }
        else if(key.equals("End The Call"))
        {
            return new EndCallAction()
        }

        throw new IllegalArgumentException("Cannot create Action from unknown key [" + key + "]")
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

        if(action == null)
        {
            throw new RuntimeException("Action [" + directive + "] for keypress [" + keypress +
                                           "] is not a valid action")
        }

        action.promptBefore = promptBefore
        action.keysPressed = keypress
    }
}
