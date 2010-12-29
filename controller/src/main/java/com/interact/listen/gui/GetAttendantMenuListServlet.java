package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.attendant.Menu;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.stats.Stat;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetAttendantMenuListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_GET_ATTENDANT_MENU_LIST);
        ServletUtil.requireLicensedFeature(ListenFeature.ATTENDANT);
        ServletUtil.requireCurrentSubscriber(request, true);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        List<Menu> results = Menu.queryAll(session);

        StringBuilder json = new StringBuilder();
        json.append("[");
        for(Menu result : results)
        {
            // TODO possible optimization here - if this list service is polled (which it probably will be),
            // there's a pretty hefty set of queries that might happen here; we might consider caching the menu
            // information to avoid this
            json.append(result.toJson(session).toJSONString());
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]");
        OutputBufferFilter.append(request, json.toString(), new JsonMarshaller().getContentType());
    }
}
