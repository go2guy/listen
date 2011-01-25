package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.GroupMember;
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

public class SaveAcdConfigurationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_SAVE_ACD_CONFIGURATION);
        ServletUtil.requireLicensedFeature(ListenFeature.ACD);
        ServletUtil.requireCurrentSubscriber(request, true);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        GroupMember.deleteAll(session);

        JSONArray groupsJson = (JSONArray)JSONValue.parse(request.getParameter("groups"));
        for(JSONObject groupJson : (List<JSONObject>)groupsJson)
        {
            String name = (String)groupJson.get("name");
            JSONArray members = (JSONArray)groupJson.get("members");
            for(JSONObject member : (List<JSONObject>)members)
            {
                Long id = (Long)member.get("id");
                Subscriber subscriber = Subscriber.queryById(session, id);
                Boolean isAdministrator = (Boolean)member.get("isAdministrator");

                GroupMember groupMember = new GroupMember();
                groupMember.setGroupName(name);
                groupMember.setSubscriber(subscriber);
                groupMember.setIsAdministrator(isAdministrator);
                session.save(groupMember);
            }
        }
    }
}
