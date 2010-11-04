package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ScheduledConference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class ScheduleConferenceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        ServletUtil.sendStat(request, Stat.GUI_SCHEDULE_CONFERENCE);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        String date = request.getParameter("date");
        if(date == null || date.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a date");
        }

        String hour = request.getParameter("hour");
        if(hour == null || hour.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an hour for the conference start time");
        }

        String minute = request.getParameter("minute");
        if(minute == null || minute.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a minute for the conference start time");
        }

        String amPm = request.getParameter("amPm");
        if(amPm == null || amPm.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an am/pm for the conference start time");
        }

        String endHour = request.getParameter("endHour");
        if(endHour == null || endHour.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an hour for the conference end time");
        }

        String endMinute = request.getParameter("endMinute");
        if(endMinute == null || endMinute.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a minute for the conference end time");
        }

        String endAmPm = request.getParameter("endAmPm");
        if(endAmPm == null || endAmPm.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an am/pm for the conference end time");
        }

        StringBuilder subjectPrepend = new StringBuilder(request.getParameter("subject"));
        if(subjectPrepend == null)
        {
            throw new BadRequestServletException("Please provide a subject for the conference invitation e-mail");
        }

        String description = request.getParameter("description");
        if(description == null)
        {
            throw new BadRequestServletException("Please provide a description");
        }

        String activeParticipants = request.getParameter("activeParticipants");
        if(activeParticipants == null)
        {
            throw new BadRequestServletException("Please provide a comma-separated list of active participants");
        }

        String passiveParticipants = request.getParameter("passiveParticipants");
        if(passiveParticipants == null)
        {
            throw new BadRequestServletException("Please provide a comma-separated list of passive participants");
        }

        if(activeParticipants.trim().length() == 0 && passiveParticipants.trim().length() == 0)
        {
            throw new BadRequestServletException("Please provide at least one active or passive participant email address");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);

        ScheduledConference scheduledConference = new ScheduledConference();
        scheduledConference.setTopic(subjectPrepend.toString());
        scheduledConference.setNotes(description);
        scheduledConference.setScheduledBy(subscriber);

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h mm a");
            String startDateTime = date + " " + hour + " " + minute + " " + amPm;
            String endDateTime = date + " " + endHour + " " + endMinute + " " + endAmPm;

            scheduledConference.setStartDate(sdf.parse(startDateTime));
            scheduledConference.setEndDate(sdf.parse(endDateTime));
        }
        catch(ParseException e)
        {
            throw new ServletException(e);
        }

        // I would think I could just get the pins directly from the first conference in the ArrayList, but that is
        // only providing one pin, for some reason. By querying explicitly, I get access to all the pins.
        ArrayList<Conference> listConferences = new ArrayList<Conference>(subscriber.getConferences());
        Long id = listConferences.get(0).getId();
        Conference conference = (Conference)persistenceService.get(Conference.class, id);
        scheduledConference.setConference(conference);

        scheduledConference.setActiveCallers(getAddressSet(activeParticipants));
        scheduledConference.setPassiveCallers(getAddressSet(passiveParticipants));

        persistenceService.save(scheduledConference);

        if(!scheduledConference.sendEmails(persistenceService))
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                             "An error occurred sending the conference schedule e-mail", "text/plain");
        }
    }

    private Set<String> getAddressSet(String parameter)
    {
        String[] addresses = parameter.split(",");
        Set<String> set = new HashSet<String>();
        // attempting some validation here so the service doesn't error out on address without an '@' sign
        for(String address : addresses)
        {
            if(address.contains("@"))
            {
                set.add(address);
            }
        }
        return set;
    }
}
