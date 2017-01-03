package com.interact.listen

import com.interact.listen.license.ListenFeature
import com.interact.listen.history.*
import com.interact.listen.attendant.MenuGroup
import com.interact.listen.pbx.NumberRoute
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import org.apache.log4j.Logger

@Secured(['ROLE_CUSTODIAN'])
class OrganizationController {
    static allowedMethods = [
        index: 'GET',
        addRoute: 'POST',
        allowedApplications: 'GET',
        create: 'GET',
        deleteRoute: 'POST',
        disable: 'POST',
        edit: 'GET',
        enable: 'POST',
        list: 'GET',
        routing: 'GET',
        save: 'POST',
        setSingleOrganization: 'POST',
        update: 'POST',
        updateRoute: 'POST'
    ]

    def applicationService
    def ldapService
    def licenseService
    def historyService
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

    // ajax
    def allowedApplications = {
        // map of organization ids to feature names
        def features = [:]
        Organization.list().each { organization ->
            features.put(organization.id, applicationService.listApplications(organization))
        }
        render features as JSON
    }

    def create = {
        render(view: 'create', model: [enableableFeatures: licenseService.enableableFeatures(), defaultRoute: grailsApplication.config.com.interact.listen.defaultOrganizationRoute])
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

    def disable = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        organization.enabled = false
        // TODO history?
        flash.successMessage = 'Organization disabled'
        redirect(action: 'list')
    }

    def edit = {
        log.debug "Edit organization entry"
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        if(organization?.outboundCallidByDid)
        {
            log.debug "Allow DID call ID enabled for organization"
        }
        else
        {
            log.debug "DID call ID not enabled for organization"
        }

	    if (!organization?.route)
	    {
		    organization.route = grailsApplication.config.com.interact.listen.defaultOrganizationRoute
	    }
        render(view: 'edit', model: [organization: organization, enableableFeatures: licenseService.enableableFeatures()])
    }

    def enable = {
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        organization.enabled = true
        // TODO history?
        flash.successMessage = 'Organization enabled'
        redirect(action: 'list')
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
        log.debug "Routing organization entry"
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'organization'
        params.order = params.order ?: 'asc'

        def total = NumberRoute.countByType(NumberRoute.Type.EXTERNAL)
        def routes = NumberRoute.withCriteria {
            eq('type', NumberRoute.Type.EXTERNAL)
            and {
                order('organization', 'asc')
                order('pattern', 'asc')
            }
        }
        
        render(view: 'routing', model: [routes: routes, routesTotal: total])
    }

    def save = {
        log.debug "Save organization entry"

        Organization.withTransaction { status ->
            def organization = new Organization()
            organization.properties['name', 'contextPath', 'outboundCallid', 'outboundCallidByDid', 'adServer', 'adDomain', 'ldapBasedn', 'ldapPort', 'ldapDc', 'route'] = params

            if(organization?.outboundCallidByDid){
                log.debug "Outbound call id by DID checked"
                organization.outboundCallidByDid=true
            }
            else {
                log.debug "Outbound call id by DID not checked"
                organization.outboundCallidByDid=false
            }

	        if (!organization?.route)
	        {
		        log.debug("No outbound route provided. Using default")
		        organization.route = grailsApplication.config.com.interact.listen.defaultOrganizationRoute
	        }
            
            params.each { k, v ->
                if(k.startsWith("enabledFeature-")) {
                     organization.addToEnabledFeatures(ListenFeature.valueOf(v))
                }
            }

            String apiKey = UUID.randomUUID().toString();
            organization.apiKey = apiKey.replaceAll('-','').substring(0,32);

            if(!organization.hasErrors() && organization.save()) {
                ldapService.addOrganization(organization)

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
            SingleOrganizationConfiguration.thisSet(organization)
        }

        redirect(action: 'list')
    }

    def update = {
        log.debug "Update organization entry [${params}]"
        def organization = Organization.get(params.id)
        if(!organization) {
            flash.errorMessage = 'Organization not found'
            redirect(action: 'list')
            return
        }

        // TODO check entity version?

        def originalExtLength = organization.extLength

        if(!params?.outboundCallidByDid){
            organization.outboundCallidByDid=false
        }

        organization.properties['name', 'contextPath', 'outboundCallid', 'outboundCallidByDid', 'extLength', 'adServer', 'adDomain', 'ldapBasedn', 'ldapPort', 'ldapDc', 'route'] = params
        
        if(organization?.outboundCallidByDid){
            log.debug "Outbound call id by DID checked [${organization?.outboundCallidByDid}]"
            organization.outboundCallidByDid=true
        }
        else {
            log.debug "Outbound call id by DID NOT checked [${organization?.outboundCallidByDid}]"
            organization.outboundCallidByDid=false
        }

	    if (!organization?.route)
	    {
		    log.debug("No outbound route provided. Using default")
		    organization.route = grailsApplication.config.com.interact.listen.defaultOrganizationRoute
	    }
        organization.enabledFeatures = []
        params.each { k, v ->
            if(k.startsWith("enabledFeature-")) {
                    organization.addToEnabledFeatures(ListenFeature.valueOf(v))
            }
        }

        if(!organization.hasErrors() && organization.save()) {
            log.debug "We've saved the organization configuration for [${organization.name}]"
            flash.successMessage = 'Organization updated'

            log.debug "Compare extension lengths [${originalExtLength}] [${organization.extLength}]"
            if(originalExtLength != organization.extLength) {
                log.debug "Saving history of ext lengths [${originalExtLength}] [${organization.extLength}]"
                historyService.changedOrganizationExtLength(organization, originalExtLength)
            }
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

