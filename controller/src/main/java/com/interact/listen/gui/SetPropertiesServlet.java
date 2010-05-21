package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class SetPropertiesServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SetPropertiesServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_SET_PROPERTIES);

        User currentUser = (User)(request.getSession().getAttribute("user"));
        if(currentUser == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        if(!currentUser.getIsAdministrator())
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
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

            Configuration.set(key, value);
            LOG.debug("Set parameter [" + name + "] to [" + request.getParameter(name) + "]");
        }
    }
}
