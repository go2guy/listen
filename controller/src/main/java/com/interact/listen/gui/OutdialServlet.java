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
import com.interact.listen.resource.*;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_OUTDIAL);

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
        
        number = removeWhitespace(number);

        LOG.debug("Outdialing to [" + number + "] for conference id [" + conferenceId + "]");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);

        Conference conference = (Conference)session.get(Conference.class, Long.valueOf(conferenceId));

        if(conference == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Conference not found", "text/plain");
        }

        if(!isSubscriberAllowedToOutdial(subscriber, conference))
        {
            throw new UnauthorizedServletException("Not allowed to outdial");
        }

        String adminSessionId = getConferenceAdminSessionId(session, conference);

        // send request to all SPOT subscribers
        List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(session);
        for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
        {
            SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi(), subscriber);
            try
            {
                String requestingNumber = ListenSpotSubscriber.firstPhoneNumber(session);
                spotSystem.outdial(number, adminSessionId, conference.getId(), requestingNumber);
            }
            catch(SpotCommunicationException e)
            {
                throw new ServletException(e);
            }
        }

        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        history.setDescription("Outdialed " + number);
        history.setSubscriber("Current subscriber");
        persistenceService.save(history);
    }

    private String getConferenceAdminSessionId(Session session, Conference conference)
    {
        Criteria criteria = session.createCriteria(Participant.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("conference", "conference_alias");
        criteria.add(Restrictions.eq("conference_alias.id", conference.getId()));
        List<Participant> participants = (List<Participant>)criteria.list();
        for(Participant participant : participants)
        {
            if(participant.getIsAdmin())
            {
                return participant.getSessionID();
            }
        }
        throw new IllegalStateException("Could not find Admin participant for Conference"); // FIXME maybe use a checked
        // exception here
    }

    private boolean isSubscriberAllowedToOutdial(Subscriber subscriber, Conference conference)
    {
        if(subscriber.getIsAdministrator())
        {
            return true;
        }

        // does the current subscriber own the conference?
        if(subscriber.getConferences().contains(conference))
        {
            return true;
        }

        return false;
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
