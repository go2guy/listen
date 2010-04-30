package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class ScheduleConferenceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

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
        Transaction transaction = session.beginTransaction();

        try
        {
            EmailerService emailService = new EmailerService();
            PersistenceService persistenceService = new PersistenceService(session);
            
            // I would think I could just get the pins directly from the first conference in the ArrayList, but that is
            // only providing one pin, for some reason. By querying explicitly, I get access to all the pins.
            ArrayList<Conference> listConferences = new ArrayList<Conference>(user.getConferences());
            Conference userConference = (Conference)persistenceService.get(Conference.class, listConferences.get(0).getId());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyyhhmma");
            String dateTime = date + hour + minute + amPm;
                        
            String[] activeAddresses = activeParticipants.split(",");
            String[] passiveAddresses = passiveParticipants.split(",");
            ArrayList<String> serviceActiveAddresses =  new ArrayList<String>();
            ArrayList<String> servicePassiveAddresses =  new ArrayList<String>();
            
            //attempting some validation here so the service doesn't error out on address without an '@' sign
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
            
            if(!serviceActiveAddresses.isEmpty())
            {
                activeSuccess = emailService.sendScheduleEmail(serviceActiveAddresses, user.getUsername(), description,
                                                               sdf.parse(dateTime), userConference, "ACTIVE");
            }
            
            if(!servicePassiveAddresses.isEmpty())
            {
                passiveSuccess = emailService.sendScheduleEmail(servicePassiveAddresses, user.getUsername(),
                                                                description, sdf.parse(dateTime), userConference,
                                                                "PASSIVE");
            }
            
            if(!(activeSuccess && passiveSuccess))
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                          "An error occurred sending the conference schedule e-mail", "text/plain");
                return;
            }

            transaction.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error scheduling conference", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: ScheduleConferenceServlet.doPost() took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }
}
