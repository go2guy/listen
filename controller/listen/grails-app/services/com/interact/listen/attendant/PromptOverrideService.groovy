package com.interact.listen.attendant

import com.interact.listen.Organization
import com.interact.listen.util.FileTypeDetector
import org.joda.time.LocalDateTime
import org.springframework.web.multipart.MultipartFile
import org.joda.time.LocalDate

class PromptOverrideService
{
    def historyService
    def promptFileService
    def springSecurityService

    // TODO fix hard-coded path
    static final File storageLocation = new File('/interact/listen/artifacts/attendant')

    PromptOverride create(PromptOverride promptOverride, MultipartFile file = null)
    {
        promptOverride.validate(['startDate', 'endDate', 'useMenu'])
        if (promptOverride.hasErrors())
        {
            log.error "promptOverride has errors [${promptOverride.errors}]"
        }

        if (file && !file.isEmpty())
        {
            def detector = new FileTypeDetector()
            def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
            if (detectedType != 'audio/x-wav')
            {
                promptOverride.optionsPrompt = file.originalFilename
                promptOverride.errors.rejectValue('optionsPrompt', 'promptOverride.optionsPrompt.must.be.wav')
            }
            else
            {
                def user = springSecurityService.getCurrentUser();
                def organization = user.organization;
                def savedFile = promptFileService.save(storageLocation, file, organization.id)
                promptOverride.optionsPrompt = file.originalFilename
            }
        }

        if (!promptOverride.hasErrors() && promptOverride.save())
        {
            historyService.createdAttendantHoliday(promptOverride)
        }
        else
        {
            log.error "promptOverride has failed with errors [${promptOverride.errors}]"
        }

        return promptOverride
    }

    PromptOverride create(def params, EventType type, MultipartFile file = null)
    {
        if(log.isDebugEnabled())
        {
            log.debug "Entering PromptOverrideService::create() with params [${params}]"
        }

        // TODO using the user organization is a bit inconsistent, since the override
        // uses the organization of its MenuGroup; in most cases they will be the same.
        // if we allow custodians to manage them, we will need to change this

        // Removing 'date' from the params because it's populated with 'struct' and this was breaking the validation.
        params.remove('date')

        def promptOverride = new PromptOverride()
        promptOverride.properties = params
        promptOverride.startDate = new LocalDateTime(params?.date_year?.toInteger(), params?.date_month?.toInteger(),
                params?.date_day?.toInteger(), params?.holidayStart_hour?.toInteger(),
                params?.holidayStart_minute?.toInteger());
        promptOverride.endDate = new LocalDateTime(params?.date_year?.toInteger(), params?.date_month?.toInteger(),
                params?.date_day?.toInteger(), params?.holidayEnd_hour?.toInteger(),
                params?.holidayEnd_minute?.toInteger());
        promptOverride.eventType = type;

        return create(promptOverride, file);
    }

    /**
     * Get events that are currently active.
     *
     * @return List of active events.
     */
    PromptOverride getCurrentEvent(Organization organization, EventType type)
    {
        LocalDateTime currentTime = new LocalDateTime();
        def c = PromptOverride.createCriteria()
        def results = c.list(max: 1) {
            le('startDate', currentTime)
            ge('endDate', currentTime)
            if(type)
            {
                eq('eventType', type)
            }
            useMenu
            {
                eq('organization', organization)
            }
            order 'startDate', 'asc'
        }

        def override;

        if(results != null && results.size() > 0)
        {
            override = results.get(0);
        }

        return override;
    }

    /**
     * Get the last event of the event type.
     *
     * @return The last event of the event type.
     */
    PromptOverride getLastEvent(Organization organization, EventType type)
    {
        def c = PromptOverride.createCriteria()
        def results = c.list(max: 1) {
            eq('eventType', type)
            useMenu
            {
                eq('organization', organization)
            }
            order 'startDate', 'desc'

        }

        def override;

        if(results != null && results.size() > 0)
        {
            override = results.get(0);
        }

        return override;
    }

    /**
     * See if an event overlaps.
     *
     * @return Event that overlaps.
     */
    PromptOverride getOverlap(LocalDateTime start, LocalDateTime end, EventType type, Organization organization)
    {
        PromptOverride returnVal = null;

        def c = PromptOverride.createCriteria()
        def results = c.list(max: 1) {
            useMenu
            {
                eq('organization', organization)
            }
            if(type)
            {
                eq('eventType', type)
            }
            or {
                and {
                    gt('startDate', start)
                    lt('startDate', end)
                }
                and {
                    gt('endDate', start)
                    lt('endDate', end)
                }
            }
        }

        if(results != null && results.size() > 0)
        {
            returnVal = results.get(0);
        }

        return returnVal;
    }

    void delete(PromptOverride promptOverride)
    {
        promptOverride.delete()
        historyService.deletedAttendantHoliday(promptOverride)
    }
}
