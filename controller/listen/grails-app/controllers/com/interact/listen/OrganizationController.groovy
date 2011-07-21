package com.interact.listen

import com.interact.listen.license.ListenFeature
import com.interact.listen.attendant.MenuGroup
import com.interact.listen.pbx.NumberRoute
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_CUSTODIAN'])
class OrganizationController {
    static allowedMethods = [
        index: 'GET',
        addRoute: 'POST',
        create: 'GET',
        delete: 'POST',
        deleteRoute: 'POST',
        edit: 'GET',
        list: 'GET',
        routing: 'GET',
        save: 'POST',
        setSingleOrganization: 'POST',
        update: 'POST',
        updateRoute: 'POST'
    ]

    def licenseService
    def userCreationService

    def index = {
        redirect(action: 'list')
    }

    def addRoute = {
        def route = new NumberRoute(params)
        route.type = NumberRoute.Type.EXTERNAL
        if(route.validate() && route.save()) {
            flash.successMessage = 'Route created'
            redirect(action: 'routing')
        } else {
            def routes = NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL, [sort: 'organization', order: 'asc'])
            render(view: 'routing', model: [routes: routes, newRoute: route])
        }
    }

    def create = {
        render(view: 'create', model: [enableableFeatures: licenseService.enableableFeatures()])
    }

    def delete = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        organization.delete()
        flash.successMessage = 'Organization deleted'
        redirect(action: 'list')
    }

    def deleteRoute = {
        if(!params.id) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        route.delete()
        flash.successMessage = 'Route deleted'
        redirect(action: 'routing')
    }

    def edit = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        render(view: 'edit', model: [organization: organization, enableableFeatures: licenseService.enableableFeatures()])
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'name'
        params.order = params.order ?: 'asc'
        def organizations = Organization.list(params)
        def total = Organization.count()
        render(view: 'list',
               model: [
                   organizationList: organizations,
                   organizationListTotal: total,
                   singleOrganization: SingleOrganizationConfiguration.retrieve()
               ]) 
    }

    def routing = {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'organization'
        params.order = params.order ?: 'asc'

        def total = NumberRoute.countByType(NumberRoute.Type.EXTERNAL)
        def routes = NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL, params)
        render(view: 'routing', model: [routes: routes, routesTotal: total])
    }

    def save = {
        // TODO this should all probably be in a service
        Organization.withTransaction { status ->
            def organization = new Organization()
            organization.properties['name', 'contextPath'] = params

            params.each { k, v ->
                if(k.startsWith("enabledFeature-")) {
                     organization.addToEnabledFeatures(ListenFeature.valueOf(v))
                }
            }

            if(!organization.hasErrors() && organization.save()) {
                def user = userCreationService.createOperator(params, organization)
                if(user.hasErrors()) {
                    status.setRollbackOnly()
                    render(view: 'create', model: [organization: organization, user: user, enableableFeatures: licenseService.enableableFeatures()])
                    return
                }

                new MenuGroup(name: 'Default', isDefault: true, organization: organization).save()

                flash.successMessage = 'Organization created'
                redirect(action: 'edit', id: organization.id)
            } else {
                status.setRollbackOnly()
                // create a temporary User object so results can be displayed back in the form
                def u = new User()
                u.properties['username', 'realName', 'emailAddress'] = params
                render(view: 'create', model: [organization: organization, user: u, enableableFeatures: licenseService.enableableFeatures()])
            }
        }
    }

    def setSingleOrganization = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.successMessage = 'Single organization disabled'
            SingleOrganizationConfiguration.unset()
        } else {
            flash.successMessage = "Single organization set to ${organization.name}"
            SingleOrganizationConfiguration.set(organization)
        }

        redirect(action: 'list')
    }

    def update = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        // TODO check entity version?
        organization.properties['name', 'contextPath'] = params

        organization.enabledFeatures = []
        params.each { k, v ->
            if(k.startsWith("enabledFeature-")) {
                    organization.addToEnabledFeatures(ListenFeature.valueOf(v))
            }
        }

        if(!organization.hasErrors() && organization.save()) {
            flash.successMessage = 'Organization updated'
            redirect(action: 'edit', params: [id: organization.id])
        } else {
            render(view: 'edit', model: [organization: organization, enableableFeatures: licenseService.enableableFeatures()])
        }
    }

    def updateRoute = {
        if(!params.id) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        route.properties = params
        if(route.validate() && route.save()) {
            flash.successMessage = 'Route updated'
            redirect(action: 'routing')
        } else {
            def routes = NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL, [sort: 'organization', order: 'asc'])
            render(view: 'routing', model: [routes: routes, updatedRoute: route])
        }
    }
}
