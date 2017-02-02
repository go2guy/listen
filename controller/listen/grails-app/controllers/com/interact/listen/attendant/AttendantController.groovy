package com.interact.listen.attendant

import com.interact.listen.User
import com.interact.listen.util.FileTypeDetector
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.LocalDateTime

//import grails.plugins.springsecurity.Secured
import org.json.simple.JSONArray
import org.json.simple.JSONValue

@Secured(['ROLE_ATTENDANT_ADMIN'])
class AttendantController {
    static allowedMethods = [
        index: 'GET',
        addHoliday: 'POST',
        deleteHoliday: 'POST',
        holidays: 'GET',
        menu: 'GET',
        save: 'POST'
    ]

    def grailsApplication
    def menuGroupService
    def promptFileService
    def promptOverrideService
    def springSecurityService

    private static final String storageLocation = "attendant";

    def index = {
        redirect(action: 'menu')
    }

    def addHoliday =
    {
        if(springSecurityService.currentUser)
        {
            User user = springSecurityService.currentUser;

            if(log.isDebugEnabled())
            {
                log.debug "addHoliday with params [${params}]"
            }

            //Check for overlap
            LocalDateTime startDate = new LocalDateTime(params?.date_year?.toInteger(), params?.date_month?.toInteger(),
                        params?.date_day?.toInteger(), params?.holidayStart_hour?.toInteger(),
                        params?.holidayStart_minute?.toInteger());
            LocalDateTime endDate = new LocalDateTime(params?.date_year?.toInteger(), params?.date_month?.toInteger(),
                        params?.date_day?.toInteger(), params?.holidayEnd_hour?.toInteger(),
                        params?.holidayEnd_minute?.toInteger());

            PromptOverride overlap = promptOverrideService.getOverlap(startDate, endDate, null, user.organization);

            if(overlap)
            {
                flash.put("errorMessage", 'Event already scheduled at this time');
                redirect(action: 'holidays');
            }
            else
            {
                def promptOverride = promptOverrideService.create(params, EventType.SCHEDULED_EVENT,
                        request.getFile('uploadedPrompt'))

                if(promptOverride.hasErrors())
                {
                    def model = promptOverrideModel(EventType.SCHEDULED_EVENT);
                    model.newPromptOverride = promptOverride
                    render(view: 'holidays', model: model)
                }
                else
                {
                    flash.successMessage = 'Holiday created'
                    redirect(action: 'holidays')
                }
            }
        }
        else
        {
            redirect(controller: 'login', action: 'auth');
        }
    }

    def updateUnscheduledEvent =
    {
        if(springSecurityService.currentUser)
        {
            User user = springSecurityService.currentUser;

            log.debug "updateUnscheduledEvent with params [${params}]"

            if(params.eventStatus.equals("Start Event"))
            {
                LocalDateTime startDate = new LocalDateTime();
                LocalDateTime endDate = new LocalDateTime().withHourOfDay(23).withMinuteOfHour(59);

                //Verify that an event isn't already ongoing
                def c = PromptOverride.createCriteria()
                def active = c.listDistinct() {
                    le('startDate', startDate)
                    ge('endDate', startDate)
                    useMenu
                    {
                        eq('organization', user.organization)
                    }
                    order 'startDate', 'asc'
                }

                if(active)
                {
                    redirect(action: 'unscheduledEvent');
                }

                PromptOverride promptOverride =
                    promptOverrideService.getLastEvent(user.organization, EventType.UNSCHEDULED_EVENT);

                if(promptOverride == null)
                {
                    promptOverride = new PromptOverride()
                }

                promptOverride.properties = params
                promptOverride.startDate = startDate;
                promptOverride.endDate = startDate.withHourOfDay(23).withMinuteOfHour(59);
                promptOverride.eventType = EventType.UNSCHEDULED_EVENT;

                //See if an event is scheduled for today. Set the end date of this event if so.
                def c2 = PromptOverride.createCriteria()
                List<PromptOverride> scheduled = c2.list(max: 1) {
                    gt('startDate', startDate)
                    le('endDate', endDate)
                    useMenu
                    {
                        eq('organization', user.organization)
                    }
                    order 'startDate', 'asc'
                }

                if(scheduled && scheduled.size() > 0)
                {
                    PromptOverride scheduledEvent = scheduled.get(0);
                    log.warn("Event scheduled for later today. Setting end date.")
                    promptOverride.endDate = scheduledEvent.startDate;
                }

                promptOverrideService.create(promptOverride, request.getFile('uploadedPrompt'));

                redirect(action: 'unscheduledEvent');
            }

            else if(params.eventStatus.equals("End Event"))
            {
                def promptOverride = PromptOverride.get(params.id)
                if(!promptOverride)
                {
                    flash.errorMessage = 'Unable to locate current event'
                    redirect(action: 'unscheduledEvent')
                    return
                }

                LocalDateTime startDate = new LocalDateTime().minusDays(1);
                LocalDateTime endDate = new LocalDateTime().minusDays(1);

                promptOverride.startDate = startDate;
                promptOverride.endDate = endDate;
                promptOverrideService.create(promptOverride, request.getFile('uploadedPrompt'), true);
                redirect(action: 'unscheduledEvent');
            }
        }
        else
        {
            redirect(controller: 'login', action: 'auth');
        }
    }

    def deleteHoliday = {
        def promptOverride = PromptOverride.get(params.id)
        if(!promptOverride) {
            flash.errorMessage = 'Holiday not found'
            redirect(action: 'holidays')
            return
        }

        promptOverrideService.delete(promptOverride)
        flash.successMessage = 'Event deleted'
        redirect(action: 'holidays')
    }

    def holidays =
    {
        render(view: 'holidays', model: promptOverrideModel(EventType.SCHEDULED_EVENT));
    }

    def unscheduledEvent =
    {
        render(view: 'unscheduledEvent', model: unscheduledEventModel());
    }

    def menu = {
        def groups = MenuGroup.findAllByOrganization(springSecurityService.currentUser.organization, [sort: 'isDefault', order: 'desc'])
        render(view: 'menu', model: [groups: groups])
    }

    def save = {
        JSONArray jsonGroups = (JSONArray)JSONValue.parse(params.groups)
        log.debug "Received menu for saving: ${jsonGroups.toJSONString()}"

        try {
            menuGroupService.saveGroups(jsonGroups)
            flash.successMessage = 'Menu saved'
            redirect(action: 'menu')
        } catch(MenuGroupValidationException e) {
            render(view: 'menu', model: [groups: e.groups])
        }
    }

    def uploadPrompt = {
        def file = request.getFile('uploadFile')
        if(!file) {
            render('Please select a file to upload')
            return
        }

        def detector = new FileTypeDetector()
        def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
        if(detectedType != 'audio/x-wav') {
            render('File must be a wav file')
            return
        }

        def user = springSecurityService.currentUser
        promptFileService.save(storageLocation, file, user.organization.id)

        render('Success')
    }

    private def promptOverrideModel(EventType type)
    {
        if(springSecurityService.currentUser)
        {
            def user = springSecurityService.currentUser
            return [
                promptOverrideList: PromptOverride.findAllByOrganizationAndEventTypeAndNotPast(user.organization, type,
                        [sort: 'date', order: 'asc'])
            ]
        }
        else
        {
            redirect(controller: 'login', action: 'auth');
        }
    }

    private def unscheduledEventModel()
    {
        if(springSecurityService.currentUser)
        {
            def user = springSecurityService.currentUser;

            def currentEvent = promptOverrideService.getCurrentEvent(user.organization, EventType.UNSCHEDULED_EVENT);
            def activeEvent = promptOverrideService.getCurrentEvent(user.organization, null);
            def lastEvent = promptOverrideService.getLastEvent(user.organization, EventType.UNSCHEDULED_EVENT);
            return [
                    currentEvent: currentEvent,
                    lastEvent: lastEvent,
                    activeEvent: activeEvent
            ]
        }
        else
        {
            redirect(controller: 'login', action: 'auth');
        }
    }
}
