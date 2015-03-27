package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.plugin.springsecurity.annotation.Secured

//import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse
import com.interact.listen.acd.*
import com.interact.listen.history.*
import org.apache.log4j.Logger

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class UserController {
    static allowedMethods = [
        index: 'GET',
        create: 'GET',
        disable: 'POST',
        delete: 'POST',
        edit: 'GET',
        list: 'GET',
        permissions: 'GET',
        save: 'POST',
        togglePermission: 'POST',
        update: 'POST'
    ]

    def licenseService
    def springSecurityService
    def userCreationService
    def userService
    def historyService

    def index = {
        redirect(action: 'list')
    }

    def create = {
        render(view: 'create')
    }

    def disable = {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        def currentUser = authenticatedUser
        if(user.id == currentUser.id) {
            flash.errorMessage = 'You cannot disable your own account'
            redirect(action: 'list')
            return
        }

        user = userService.disable(user)
        if(user.hasErrors()) {
            flash.errorMessage = 'An error occurred disabling the user'
        } else {
            flash.successMessage = 'User disabled'
        }
        redirect(action: 'list')
    }

    def delete = {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        def currentUser = authenticatedUser
        if(user.id == currentUser.id) {
            flash.errorMessage = 'You cannot delete your own account'
            redirect(action: 'list')
            return
        }

        if (userService.deleteUser(user)) {
            userService.cleanUpAudio(user)
            flash.successMessage = 'User deleted'
        } else {
            flash.errorMessage = 'An error occurred deleting the user'
        }
        
        redirect(action: 'list')
        return
    }
    
    def edit =
    {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        if(log.isDebugEnabled())
        {
            log.debug "Lets edit user [${user.username}]"
        }

        def userSkills;
        
        if(user.hasRole("ROLE_ACD_USER"))
        {
            Organization organization = user.organization;
            def skillList = Skill.findAllByOrganization(organization, [sort: 'skillname', order: 'asc']);
            def userSkills_ = UserSkill.findAllByUser(user)

            def uSkill = [:]
            userSkills = []
            for(Skill skill : skillList)
            {
                uSkill = [:]
                uSkill.user = user
                uSkill.skillname = skill.skillname
                uSkill.description = skill.description
                uSkill.id = skill.id;
                uSkill.selected = false;
                uSkill.priority = "6";

                for(UserSkill userSkill : userSkills_)
                {
                    if(userSkill.skillId == skill.id )
                    {
                        uSkill.selected = true
                        uSkill.priority = userSkill.priority.toString();
                        if(log.isDebugEnabled())
                        {
                            log.debug("User has skill[" + skill.description + "], priority[" + userSkill.priority + "]");
                        }
                        break;
                    }
                }
                userSkills.add(uSkill);
            }
        }
        
        render(view: 'edit', 
            model: [user: user, 
            userSkills: userSkills])
    }

    def enable = {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        user = userService.enable(user)
        if(user.hasErrors()) {
            flash.errorMessage = 'An error occurred enabling the user'
        } else {
            flash.successMessage = 'User enabled'
        }
        redirect(action: 'list')
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'username'
        params.order = params.order ?: 'asc'
        def organization = authenticatedUser?.organization
        def userList = User.createCriteria().list(params) {
            eq('organization', organization)
            if(!licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY)) {
                eq('isActiveDirectory', false)
            }
        }

        def userListTotal = User.createCriteria().get {
            projections {
                count('id')
            }
            eq('organization', organization)
            if(!licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY)) {
                eq('isActiveDirectory', false)
            }
        }
        render(view: 'list', model: [userList: userList, userListTotal: userListTotal])
    }

    def permissions = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'username'
        params.order = params.order ?: 'asc'
        def organization = authenticatedUser?.organization
        def userList = User.createCriteria().list(params) {
            eq('organization', organization)
            if(!licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY)) {
                eq('isActiveDirectory', false)
            }
        }

        def userListTotal = User.createCriteria().get {
            projections {
                count('id')
            }
            eq('organization', organization)
            if(!licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY)) {
                eq('isActiveDirectory', false)
            }
        }
        render(view: 'permissions', model: [available: AssignablePermission.values().sort { it.description }, userList: userList, userListTotal: userListTotal])
    }

    def save = {
        log.debug "Save User params [${params}]"
        def user = userCreationService.createUser(params, authenticatedUser.organization)
        if(user.hasErrors()) {
            render(view: 'create', model: [user: user])
        } else {
            flash.successMessage = 'User Created'
            redirect(action: 'edit', params: [id: user.id])
        }
    }

    def togglePermission = {
        log.debug "togglePermission with params [${params}]"
        def user = User.get(params.id)
        if(!user) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 'User not found')
            return
        }

        def permission = AssignablePermission.valueOf(params.permission)
        if(!permission) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, 'Permission not found')
            return
        }

        def role = Role.findByAuthority(permission.authority)
        if(!role) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 'Permission/authority mismatch')
            return
        }

        if(authenticatedUser == user) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        if(UserRole.findByUserAndRole(user, role)) {
            log.debug "Removing user role [${permission.description}]"
            UserRole.remove(user, role, true)
            historyService.removeUserPermission(user, permission.description);
        } else {
            log.debug "Adding user role [${permission.description}]"
            UserRole.create(user, role, true)
            historyService.addUserPermission(user, permission.description);
        }

        render(contentType: 'application/json') {
            hasRole = UserRole.findByUserAndRole(user, role) as boolean
        }
    }

    def update =
    {
        if(log.isDebugEnabled())
        {
            log.debug "Update User params [${params}]"
        }

        def user = User.get(params.id)
        if(!user)
        {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        user = userService.update(user, params, true)

        if(user.hasErrors())
        {
            render(view: 'edit', model: [user: user])
        }
        else
        {
            flash.successMessage = 'User updated'
            redirect(action: 'edit', params: [id: user.id])
        }
    }
}
