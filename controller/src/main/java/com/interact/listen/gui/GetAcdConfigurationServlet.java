package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.GroupMember;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GetAcdConfigurationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_GET_ACD_CONFIGURATION);
        ServletUtil.requireLicensedFeature(ListenFeature.ACD);
        ServletUtil.requireCurrentSubscriber(request, true);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Map<String, Set<GroupMember>> groups = GroupMember.queryAllInGroups(session);

        JSONArray groupsJson = new JSONArray();
        for(Map.Entry<String, Set<GroupMember>> entry : groups.entrySet())
        {
            JSONArray membersJson = new JSONArray();
            for(GroupMember member : entry.getValue())
            {
                JSONObject memberJson = new JSONObject();
                memberJson.put("id", member.getSubscriber().getId());
                memberJson.put("isAdministrator", member.getIsAdministrator());
                membersJson.add(memberJson);
            }

            JSONObject groupJson = new JSONObject();
            groupJson.put("name", entry.getKey());
            groupJson.put("members", membersJson);

            groupsJson.add(groupJson);
        }

        List<Subscriber> subscribers = Subscriber.queryAllAlphabeticallyByRealName(session);
        JSONArray subscribersJson = new JSONArray();
        for(Subscriber subscriber : subscribers)
        {
            JSONObject subscriberJson = new JSONObject();
            subscriberJson.put("id", subscriber.getId());
            subscriberJson.put("name", subscriber.friendlyName());
            subscribersJson.add(subscriberJson);
        }

        JSONObject responseJson = new JSONObject();
        responseJson.put("eligible", subscribersJson);
        responseJson.put("groups", groupsJson);

        OutputBufferFilter.append(request, responseJson.toString(), new JsonMarshaller().getContentType());
    }
}
