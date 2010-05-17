package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

public class ScheduleConferenceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }

        statSender.send(Stat.GUI_SCHEDULE_CONFERENCE);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        String date = request.getParameter("date");
        if(date == null || date.equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a date",
                                      "text/plain");
            return;
        }

        String hour = request.getParameter("hour");
        if(hour == null || hour.equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide an hour for the conference start time", "text/plain");
            return;
        }

        String minute = request.getParameter("minute");
        if(minute == null || minute.equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide a minute for the conference start time", "text/plain");
            return;
        }

        String amPm = request.getParameter("amPm");
        if(amPm == null || amPm.equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide an am/pm for the conference start time", "text/plain");
            return;
        }

        StringBuilder subjectPrepend = new StringBuilder(request.getParameter("subject"));
        if(subjectPrepend == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide a subject for the conference invitiation e-mail", "text/plain");
            return;
        }

        if(subjectPrepend.length() > 0)
        {
            // Only want the dash and spaces if something is actually going to be prepended
            subjectPrepend.append(" - ");
        }

        String description = request.getParameter("description");
        if(description == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a description",
                                      "text/plain");
            return;
        }

        String activeParticipants = request.getParameter("activeParticipants");
        if(activeParticipants == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide a comma-separated list of active participants", "text/plain");
            return;
        }

        String passiveParticipants = request.getParameter("passiveParticipants");
        if(passiveParticipants == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide a comma-separated list of passive participants", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        EmailerService emailService = new EmailerService();
        PersistenceService persistenceService = new PersistenceService(session);

        // I would think I could just get the pins directly from the first conference in the ArrayList, but that is
        // only providing one pin, for some reason. By querying explicitly, I get access to all the pins.
        ArrayList<Conference> listConferences = new ArrayList<Conference>(user.getConferences());
        Long id = listConferences.get(0).getId();
        Conference userConference = (Conference)persistenceService.get(Conference.class, id);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h mm a");
        String dateTime = date + " " + hour + " " + minute + " " + amPm;

        String[] activeAddresses = activeParticipants.split(",");
        String[] passiveAddresses = passiveParticipants.split(",");
        ArrayList<String> serviceActiveAddresses = new ArrayList<String>();
        ArrayList<String> servicePassiveAddresses = new ArrayList<String>();

        List<Resource> listenSpotSubscribers = queryListenSpotSubscribers(session);
        String phoneNumber = getConferencePhoneNumber(listenSpotSubscribers);
        String protocol = getConferenceProtocol(listenSpotSubscribers);

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
            activeSuccess = emailService.sendScheduleEmail(serviceActiveAddresses, user.getUsername(), description,
                                                           parsedDate, userConference, phoneNumber, protocol,
                                                           subjectPrepend.toString(), "ACTIVE");
        }

        if(!servicePassiveAddresses.isEmpty())
        {
            passiveSuccess = emailService.sendScheduleEmail(servicePassiveAddresses, user.getUsername(), description,
                                                            parsedDate, userConference, phoneNumber, protocol,
                                                            subjectPrepend.toString(), "PASSIVE");
        }
        if(!(activeSuccess && passiveSuccess))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "An error occurred sending the conference schedule e-mail", "text/plain");
            return;
        }

    }

    private List<Resource> queryListenSpotSubscribers(Session session)
    {
        List<Resource> listenSpotSubscribers;

        Criteria criteria = session.createCriteria(ListenSpotSubscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        listenSpotSubscribers = (List<Resource>)criteria.list();

        return listenSpotSubscribers;
    }

    private String getConferencePhoneNumber(List<Resource> listenSpotSubscribers)
    {
        String phoneNumber = "";

        if(listenSpotSubscribers.size() > 0)
        {
            phoneNumber = ((ListenSpotSubscriber)listenSpotSubscribers.get(0)).getPhoneNumber();
        }

        return phoneNumber;
    }

    private String getConferenceProtocol(List<Resource> listenSpotSubscribers)
    {
        String protocol = "";

        if(listenSpotSubscribers.size() > 0)
        {
            // get the enum value as a string
            protocol = ((ListenSpotSubscriber)listenSpotSubscribers.get(0)).getPhoneNumberProtocol().toString();
        }

        return protocol;
    }
}
