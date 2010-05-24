package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class OutdialServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private OutdialServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new OutdialServlet();
    }

    @Test
    public void test_doPost_withNoSessionUser_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("user") == null;

        request.setMethod("POST");
        request.setParameter("conferenceId", randomString());
        request.setParameter("number", randomString() + "foo");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("Unauthorized", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("conferenceId", (String)null);
        request.setParameter("number", randomString() + "foo");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a conferenceId", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("conferenceId", " ");
        request.setParameter("number", randomString() + "foo");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a conferenceId", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("conferenceId", randomString());
        request.setParameter("number", (String)null);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("conferenceId", randomString());
        request.setParameter("number", " ");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withConferenceNotFound_throwsListenServletExceptionWithBadRequest() throws ServletException, IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("conferenceId", randomString()); // hopefully doesn't exist
        request.setParameter("number", randomString() + "foo");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Conference not found", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }

    // TODO this is used in several servlets - refactor it into some test utility class
    private void setSessionUser(HttpServletRequest request, boolean isAdmin)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setIsAdministrator(isAdmin);
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }

    // TODO refactor this out into test utils
    private String randomString()
    {
        return String.valueOf(System.currentTimeMillis());
    }
}
