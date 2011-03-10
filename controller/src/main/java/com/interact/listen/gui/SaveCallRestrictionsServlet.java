package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.CallRestriction;
import com.interact.listen.resource.CallRestriction.Directive;
import com.interact.listen.resource.Subscriber;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SaveCallRestrictionsServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(SaveCallRestrictionsServlet.class);
    private static final long serialVersionUID = 1L;

    private enum Target
    {
        EVERYONE, EVERYONE_EXCEPT, SUBSCRIBERS;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        JSONArray array = (JSONArray)JSONValue.parse(request.getParameter("restrictions"));

        CallRestriction.deleteAll(session);

        for(JSONObject obj : (List<JSONObject>)array)
        {
            String destination = (String)obj.get("destination");
            Target target = Target.valueOf((String)obj.get("target"));
            List<String> subscribers = (List<String>)obj.get("subscribers");

            LOG.debug("destination: " + destination);
            LOG.debug("target: " + target);
            LOG.debug("subscriber count: " + subscribers.size());

            if(destination.trim().equals(""))
            {
                LOG.warn("Destination is blank, ignoring");
                continue;
            }

            if((target == Target.EVERYONE_EXCEPT || target == Target.SUBSCRIBERS) && subscribers.size() == 0)
            {
                LOG.debug("Zero subscribers for target " + target + ", ignoring");
                continue;
            }

            if(target == Target.EVERYONE || target == Target.EVERYONE_EXCEPT)
            {
                CallRestriction restriction = new CallRestriction();
                restriction.setDestination(destination);
                restriction.setDirective(Directive.DENY);
                restriction.setForEveryone(true);
                ps.save(restriction);
            }
            
            if(target != Target.EVERYONE)
            {
                for(String username : subscribers)
                {
                    Subscriber targetSubscriber = Subscriber.queryByUsername(session, username);
                    if(targetSubscriber == null)
                    {
                        LOG.debug("No target subscriber found with username [" + username + "], ignoring");
                        continue;
                    }

                    CallRestriction restriction = new CallRestriction();
                    restriction.setDestination(destination);
                    restriction.setDirective(target == Target.EVERYONE_EXCEPT ? Directive.ALLOW : Directive.DENY);
                    restriction.setForEveryone(false);
                    restriction.setSubscriber(targetSubscriber);
                    ps.save(restriction);
                }
            }
        }
    }
}
