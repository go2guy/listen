package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EditPagerServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private EditPagerServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new EditPagerServlet();
    }
    
    @Test
    public void test_doPost_withAlternateNumber_updatesAlternateNumber() throws ServletException,
        IOException
    {
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        setSessionSubscriber(request, false);

        try
        {
            request.setMethod("POST");
            request.setParameter("alternateNumber", "123456789");
            
            servlet.service(request, response);

            assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "123456789");
        }
        finally
        {
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_blankAlternateNumber_updatesAlternateNumber() throws ServletException,
        IOException
    {
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        setSessionSubscriber(request, false);

        try
        {
            request.setMethod("POST");
            request.setParameter("alternateNumber", "");
            
            servlet.service(request, response);

            assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "");
        }
        finally
        {
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
            tx.commit();
        }
    }
    
    private void setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
    {
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());
        subscriber.setIsAdministrator(isAdministrator);
        
        hibernateSession.save(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("subscriber", subscriber);
    }
}
