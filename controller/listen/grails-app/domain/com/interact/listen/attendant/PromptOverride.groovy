package com.interact.listen.attendant

import com.interact.listen.Organization
import org.apache.log4j.Logger
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

class PromptOverride
{
    private static final Logger _log =
        Logger.getLogger("grails.app.controllers.com.interact.listen.attendant.PromptOverride")

    LocalDateTime startDate
    LocalDateTime endDate
    String optionsPrompt
    MenuGroup useMenu
    EventType eventType;

    static constraints =
    {
        optionsPrompt blank: false
    }

    static def findAllByOrganizationAndEventTypeAndNotPast(Organization organization, EventType type, def params = [:])
    {
        def today =
            new LocalDateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        if(_log.isDebugEnabled())
        {
            _log.debug "Finding all PromptOverride instances with organization [${organization.name}] and params: ${params}"
        }

        def c = PromptOverride.createCriteria()
        def result = c.listDistinct() {
            ge('startDate', today)
            eq('eventType', type)
            order 'startDate', 'asc'
        }

        if(_log.isDebugEnabled())
        {
            _log.debug "  Found ${result.size()} PromptOverride instances"
        }
        return result
    }
}

/**
 * Available values for the types of events.
 */
enum EventType
{
    SCHEDULED_EVENT,
    UNSCHEDULED_EVENT
}
