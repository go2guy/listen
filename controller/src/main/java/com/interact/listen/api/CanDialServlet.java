package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.Subscriber;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.simple.JSONObject;

public class CanDialServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(CanDialServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        String href = ServletUtil.getNotNullNotEmptyString("subscriber", request, "subscriber");
        String destination = ServletUtil.getNotNullNotEmptyString("destination", request, "destination");
        Long id = Marshaller.getIdFromHref(href);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriber = Subscriber.queryById(session, id);
        if(subscriber == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }
        
        boolean canDial = subscriber.canDial(session, destination);
        
        JSONObject jsonReturn = new JSONObject();
        jsonReturn.put("canDial", canDial);
        
        OutputBufferFilter.append(request, jsonReturn.toString(), "application/json");
    }
}
