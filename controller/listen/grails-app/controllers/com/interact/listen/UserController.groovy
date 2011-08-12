package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class UserController {
    static allowedMethods = [
        index: 'GET',
        create: 'GET',
        disable: 'POST',
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

    def edit = {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        render(view: 'edit', model: [user: user])
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
        def user = userCreationService.createUser(params, authenticatedUser.organization)
        if(user.hasErrors()) {
            render(view: 'create', model: [user: user])
        } else {
            flash.successMessage = 'User Created'
            redirect(action: 'edit', params: [id: user.id])
        }
    }

    def togglePermission = {
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
            UserRole.remove(user, role, true)
        } else {
            UserRole.create(user, role, true)
        }

        render(contentType: 'application/json') {
            hasRole = UserRole.findByUserAndRole(user, role) as boolean
        }
    }

    def update = {
        def user = User.get(params.id)
        if(!user) {
            flash.errorMessage = 'User not found'
            redirect(action: 'list')
            return
        }

        user = userService.update(user, params, true)
        if(user.hasErrors()) {
            render(view: 'edit', model: [user: user])
        } else {
            flash.successMessage = 'User updated'
            redirect(action: 'edit', params: [id: user.id])
        }
    }
}
