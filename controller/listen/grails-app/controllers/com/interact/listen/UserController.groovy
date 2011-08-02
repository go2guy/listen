package com.interact.listen

import com.interact.listen.license.ListenFeature
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class UserController {
    static allowedMethods = [
        index: 'GET',
        create: 'GET',
        disable: 'POST',
        edit: 'GET',
        list: 'GET',
        save: 'POST',
        update: 'POST'
    ]

    def licenseService
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

    def save = {
        def user = userCreationService.createUser(params, authenticatedUser.organization)
        if(user.hasErrors()) {
            render(view: 'create', model: [user: user])
        } else {
            flash.successMessage = 'User Created'
            redirect(action: 'edit', params: [id: user.id])
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
