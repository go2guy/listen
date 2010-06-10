package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of Users.
 */
public class GetUserListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_USER_LIST);

        if(!user.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<User> users = (List<User>)criteria.list();

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("[");
        for(User u : users)
        {
            json.append("{");
            json.append("\"id\":").append(u.getId()).append(",");

            String username = marshaller.convert(String.class, u.getUsername());
            json.append("\"username\":\"").append(username).append("\",");

            String lastLogin = marshaller.convert(Date.class, u.getLastLogin());
            json.append("\"lastLogin\":\"").append(lastLogin).append("\"");

            if(u.getSubscriber() != null)
            {
                json.append(",");                
                String subscriber = marshaller.convert(String.class, u.getSubscriber().getNumber());
                json.append("\"subscriber\":\"").append(subscriber).append("\"");
            }

            json.append("},");
        }
        if(users.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]");

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }
}
