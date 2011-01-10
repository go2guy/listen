package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;

public class GetFindMeConfigurationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.FINDME))
        {
            throw new NotLicensedException(ListenFeature.FINDME);
        }

        ServletUtil.sendStat(request, Stat.GUI_GET_FINDME_CONFIGURATION);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        TreeMap<Integer, Set<FindMeNumber>> groups = FindMeNumber.queryBySubscriberInPriorityGroups(session,
                                                                                                    subscriber, true);

        JSONArray json = FindMeNumber.groupsToJson(groups);
        OutputBufferFilter.append(request, json.toJSONString(), "application/json");
    }
}
