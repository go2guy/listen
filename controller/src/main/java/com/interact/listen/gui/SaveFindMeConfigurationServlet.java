package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SaveFindMeConfigurationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.FINDME))
        {
            throw new NotLicensedException(ListenFeature.FINDME);
        }

        ServletUtil.sendStat(request, Stat.GUI_SAVE_FINDME_CONFIGURATION);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        JSONArray groups = (JSONArray)JSONValue.parse(request.getParameter("findme"));

        for(JSONArray group : (List<JSONArray>)groups)
        {
            System.out.println("GROUP ---");
            for(JSONObject dial : (List<JSONObject>)group)
            {
                System.out.println("   number:   " + dial.get("number"));
                System.out.println("   duration: " + dial.get("duration"));
                System.out.println("   enabled:  " + dial.get("enabled"));
            }
        }
    }
}
