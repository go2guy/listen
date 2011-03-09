package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.CallRestriction;
import com.interact.listen.resource.CallRestriction.Directive;
import com.interact.listen.resource.Subscriber;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GetCallRestrictionsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
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
        Map<String, List<CallRestriction>> destinations = CallRestriction.queryAllGroupedByDestination(session);

        JSONArray list = new JSONArray();

        // this loop makes some assumptions about how the configuration was saved;
        // we assume that it was saved only through SaveCallRestrictionsServlet, and that
        // if there are per-subscriber restrictions, they are all consistent with each other.
        for(Map.Entry<String, List<CallRestriction>> entry : destinations.entrySet())
        {
            JSONObject json = new JSONObject();
            json.put("destination", entry.getKey());

            JSONArray subscribers = new JSONArray();

            for(CallRestriction restriction : entry.getValue())
            {
                if(restriction.getForEveryone())
                {
                    json.put("target", "EVERYONE");
                }
                else if(restriction.getDirective() == Directive.ALLOW)
                {
                    json.put("target", "EVERYONE_EXCEPT");
                }
                else
                {
                    json.put("target", "SUBSCRIBERS");
                }

                if(restriction.getSubscriber() != null)
                {
                    subscribers.add(restriction.getSubscriber().getUsername());
                }
            }

            json.put("subscribers", subscribers);
            list.add(json);
        }

        OutputBufferFilter.append(request, list.toJSONString(), "application/json");
    }
}
