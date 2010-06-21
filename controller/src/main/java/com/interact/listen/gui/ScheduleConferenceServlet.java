package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ListenSpotSubscriber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }

        statSender.send(Stat.GUI_SCHEDULE_CONFERENCE);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        String date = request.getParameter("date");
        if(date == null || date.equals(""))
        {
            throw new BadRequestServletException("Please provide a date");
        }

        String hour = request.getParameter("hour");
        if(hour == null || hour.equals(""))
        {
            throw new BadRequestServletException("Please provide an hour for the conference start time");
        }

        String minute = request.getParameter("minute");
        if(minute == null || minute.equals(""))
        {
            throw new BadRequestServletException("Please provide a minute for the conference start time");
        }

        String amPm = request.getParameter("amPm");
        if(amPm == null || amPm.equals(""))
        {
            throw new BadRequestServletException("Please provide an am/pm for the conference start time");
        }

        StringBuilder subjectPrepend = new StringBuilder(request.getParameter("subject"));
        if(subjectPrepend == null)
        {
            throw new BadRequestServletException("Please provide a subject for the conference invitation e-mail");
        }

        if(subjectPrepend.length() > 0)
        {
            // Only want the dash and spaces if something is actually going to be prepended
            subjectPrepend.append(" - ");
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

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        EmailerService emailService = new EmailerService();
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);

        // I would think I could just get the pins directly from the first conference in the ArrayList, but that is
        // only providing one pin, for some reason. By querying explicitly, I get access to all the pins.
        ArrayList<Conference> listConferences = new ArrayList<Conference>(subscriber.getConferences());
        Long id = listConferences.get(0).getId();
        Conference subscriberConference = (Conference)persistenceService.get(Conference.class, id);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h mm a");
        String dateTime = date + " " + hour + " " + minute + " " + amPm;

        String[] activeAddresses = activeParticipants.split(",");
        String[] passiveAddresses = passiveParticipants.split(",");
        ArrayList<String> serviceActiveAddresses = new ArrayList<String>();
        ArrayList<String> servicePassiveAddresses = new ArrayList<String>();

        String phoneNumber = ListenSpotSubscriber.getFirstPhoneNumber(session);
        String protocol = ListenSpotSubscriber.getFirstProtocol(session);

        if(phoneNumber.equals(""))
        {
            // ***
            // Current implementation is a person has to have called into the spot system before the phone number is
            // available
            // We should still send the invites in my opinion
            // ***
            phoneNumber = "Contact the conference administrator for access number";

            // want the label to say "Phone Number" in the e-mail when phone number is above message
            protocol = "PSTN";
        }

        // attempting some validation here so the service doesn't error out on address without an '@' sign
        for(String activeAddress : activeAddresses)
        {
            if(activeAddress.contains("@"))
            {
                serviceActiveAddresses.add(activeAddress);
            }
        }

        for(String passiveAddress : passiveAddresses)
        {
            if(passiveAddress.contains("@"))
            {
                servicePassiveAddresses.add(passiveAddress);
            }
        }

        boolean activeSuccess = true;
        boolean passiveSuccess = true;

        Date parsedDate;
        try
        {
            parsedDate = sdf.parse(dateTime);
        }
        catch(ParseException e)
        {
            throw new ServletException(e);
        }

        if(!serviceActiveAddresses.isEmpty())
        {
            activeSuccess = emailService.sendScheduleEmail(serviceActiveAddresses, subscriber.getUsername(), description,
                                                           parsedDate, subscriberConference, phoneNumber, protocol,
                                                           subjectPrepend.toString(), "ACTIVE");
        }

        if(!servicePassiveAddresses.isEmpty())
        {
            passiveSuccess = emailService.sendScheduleEmail(servicePassiveAddresses, subscriber.getUsername(), description,
                                                            parsedDate, subscriberConference, phoneNumber, protocol,
                                                            subjectPrepend.toString(), "PASSIVE");
        }

        if(!(activeSuccess && passiveSuccess))
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                             "An error occurred sending the conference schedule e-mail", "text/plain");
        }
    }
}
