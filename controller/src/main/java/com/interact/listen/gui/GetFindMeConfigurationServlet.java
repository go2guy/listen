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

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        List<FindMeNumber> results = FindMeNumber.queryBySubscriberOrderByPriority(session, subscriber);

        // sort into groups by priority
        Map<Integer, List<FindMeNumber>> groups = new TreeMap<Integer, List<FindMeNumber>>();
        for(FindMeNumber result : results)
        {
            if(groups.get(result.getPriority()) == null)
            {
                groups.put(result.getPriority(), new ArrayList<FindMeNumber>());
            }
            groups.get(result.getPriority()).add(result);
        }

        // turn sorted group map into json
        JSONArray json = new JSONArray();
        for(List<FindMeNumber> group : groups.values())
        {
            JSONArray jsonGroup = new JSONArray();
            for(FindMeNumber number : group)
            {
                JSONObject jsonNumber = new JSONObject();
                jsonNumber.put("number", number.getNumber());
                jsonNumber.put("duration", number.getDialDuration());
                jsonNumber.put("enabled", number.getEnabled());
                jsonGroup.add(jsonNumber);
            }
            json.add(jsonGroup);
        }

        OutputBufferFilter.append(request, json.toJSONString(), "application/json");
    }
}
