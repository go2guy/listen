package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetPropertiesServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_PROPERTIES);

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

        StringBuilder json = new StringBuilder();
        json.append("{");
        for(Property.Key key : Property.Key.values())
        {
            json.append("\"").append(key.getKey()).append("\":");
            json.append("\"").append(Configuration.get(key)).append("\"");
            json.append(",");
        }
        if(Property.Key.values().length > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("}");
        ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, json.toString(), "application/json");
    }
}
