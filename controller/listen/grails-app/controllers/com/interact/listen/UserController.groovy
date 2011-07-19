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

    def cloudToDeviceService
    def historyService
    def licenseService
    def springSecurityService
    def userCreationService
    def userDeletionService

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

        def currentUser = springSecurityService.getCurrentUser()
        if(user.id == currentUser.id) {
            flash.errorMessage = 'You cannot disable your own account'
            redirect(action: 'list')
            return
        }

        user.enabled = false
        user.save()

        historyService.disabledUser(user)

        flash.successMessage = 'User disabled'
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

        user.enabled = true
        user.save()

        historyService.enabledUser(user)

        flash.successMessage = 'User enabled'
        redirect(action: 'list')
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'username'
        params.order = params.order ?: 'asc'
        def organization = springSecurityService.getCurrentUser()?.organization
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
        def user = userCreationService.createUser(params, springSecurityService.getCurrentUser().organization)
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

        // TODO check entity version?
        user.properties['username', 'pass', 'confirm', 'realName', 'emailAddress'] = params
        if(user.pass?.trim()?.length() > 0) {
            user.password = springSecurityService.encodePassword(user.pass)
        }
        if(user.validate() && user.save()) {
            cloudToDeviceService.sendContactSync()
            flash.successMessage = 'User updated'
            redirect(action: 'edit', params: [id: user.id])
        } else {
            render(view: 'edit', model: [user: user])
        }
    }
}
