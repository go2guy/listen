package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.ConferenceHistory;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class OutdialServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(OutdialServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        ServletUtil.sendStat(request, Stat.GUI_OUTDIAL);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        String conferenceId = request.getParameter("conferenceId");
        if(conferenceId == null || conferenceId.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a conferenceId");
        }

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a number",
                                             "text/plain");
        }

        String interrupt = request.getParameter("interrupt");
        if(interrupt == null || interrupt.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a interrupt decision",
                                             "text/plain");
        }
        
        number = removeWhitespace(number);

        LOG.debug("Outdialing to [" + number + "] for conference id [" + conferenceId + "] with admin interrupt of [" + interrupt + "]");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);

        Conference conference = (Conference)session.get(Conference.class, Long.valueOf(conferenceId));

        if(conference == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Conference not found", "text/plain");
        }

        if(!subscriber.ownsConference(conference))
        {
            throw new UnauthorizedServletException("Not allowed to outdial");
        }

        String adminSessionId = conference.firstAdminSessionId(session);

        SpotSystem spotSystem = new SpotSystem(subscriber);
        try
        {
            String requestingNumber = Configuration.phoneNumber();
            if(Boolean.valueOf(interrupt))
            {
                spotSystem.interactiveOutdial(number, adminSessionId, requestingNumber);
            }
            else
            {
                spotSystem.outdial(number, adminSessionId, conference.getId(), requestingNumber);
            }
        }
        catch(SpotCommunicationException e)
        {
            throw new ServletException(e);
        }

        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        history.setDescription("Outdialed " + number);
        history.setSubscriber("Current subscriber");
        persistenceService.save(history);
    }

    private String removeWhitespace(String theString)
    {
        char[] theChars = theString.toCharArray();
        StringBuilder returnString = new StringBuilder();
        
        for(int i = 0; i < theChars.length; i++)
        {
            if(theChars[i] != ' ')
            {
                returnString.append(theChars[i]);
            }
        }
        
        return returnString.toString();
    }
}
