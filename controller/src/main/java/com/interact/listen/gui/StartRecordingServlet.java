package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class StartRecordingServlet extends HttpServlet
{
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
        statSender.send(Stat.GUI_START_RECORDING);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new UnauthorizedServletException();
        }

        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Conference conference = (Conference)session.get(Conference.class, Long.valueOf(id));
        
        // System admins and owners of the conference are the only ones that can start recording.
        if(!user.getIsAdministrator() && !(user.getConferences().contains(conference)))
        {
            throw new UnauthorizedServletException("Not allowed to start recording");
        }
        
        if(!conference.getIsStarted())
        {
            throw new BadRequestServletException("Conference must be started for recording");
        }
        
        String adminSessionId = getConferenceAdminSessionId(session, conference);

        // send request to all SPOT subscribers
        List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(session);
        for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
        {
            SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
            try
            {
                spotSystem.startRecording(conference, adminSessionId);
            }
            catch(SpotCommunicationException e)
            {
                throw new ServletException(e);
            }
        }
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
}
