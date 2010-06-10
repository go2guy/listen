package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of Users.
 */
public class GetUserServlet extends HttpServlet
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
        statSender.send(Stat.GUI_GET_USER);

        if(!user.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }

        if(request.getParameter("id") == null || request.getParameter("id").trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        User u = (User)session.get(User.class, Long.parseLong(request.getParameter("id")));

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        String content = marshalUserToJson(u, marshaller);

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }

    public static String marshalUserToJson(User user, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(user.getId()).append(",");

        String username = marshaller.convert(String.class, user.getUsername());
        json.append("\"username\":\"").append(username).append("\",");

        String lastLogin = marshaller.convert(Date.class, user.getLastLogin());
        json.append("\"lastLogin\":\"").append(lastLogin).append("\"");

        if(user.getSubscriber() != null)
        {
            json.append(",");
            String number = marshaller.convert(String.class, user.getSubscriber().getNumber());
            json.append("\"number\":\"").append(number).append("\"");
        }

        json.append("}");
        return json.toString();
    }
}
