package com.interact.listen.gui;

import com.interact.listen.api.GetDnisServlet;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SetPropertiesServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SetPropertiesServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_SET_PROPERTIES);

        Subscriber currentSubscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        for(String name : (List<String>)Collections.list(request.getParameterNames()))
        {
            String value = request.getParameter(name);
            Property.Key key = Property.Key.findByKey(name);
            if(key == null)
            {
                LOG.warn("Tried to set unrecognized parameter [" + name + "] to [" + value + "]; ignoring");
                continue;
            }

            if(key.equals(Property.Key.DNIS_MAPPING))
            {
                Map<String, String> mappings = GetDnisServlet.dnisConfigurationToMap(value);
                for(Map.Entry<String, String> entry : mappings.entrySet())
                {
                    if(entry.getKey().contains("*") && entry.getKey().indexOf("*") != entry.getKey().length() - 1)
                    {
                        throw new BadRequestServletException("Wildcard (*) may only be at the end of mapping " +
                                                             entry.getKey());
                    }
                }

                List<String> mappingKeys = GetDnisServlet.dnisConfigurationKeys(value);
                Set<String> verify = new HashSet<String>(mappingKeys.size());
                for(String mappingKey : mappingKeys)
                {
                    if(verify.contains(mappingKey))
                    {
                        throw new BadRequestServletException("Mapping [" + mappingKey + "] cannot be defined twice");
                    }
                    verify.add(mappingKey);
                }
            }

            Configuration.set(key, value);
            LOG.debug("Set parameter [" + name + "] to [" + request.getParameter(name) + "]");
        }
    }
}
