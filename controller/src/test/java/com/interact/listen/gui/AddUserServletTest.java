package com.interact.listen.gui;

import static org.junit.Assert.*;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.resource.*;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AddUserServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AddUserServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new AddUserServlet();
    }

    @Test
    public void test_doPost_withValidParameters_provisionsNewAccount() throws ServletException, IOException
    {
        setSessionUser(request, true); // admin user

        final String number = randomString();
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;

        request.setMethod("POST");
        request.setParameter("number", number);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        servlet.service(request, response);

        // verify a subscriber was created with the provided number
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);

        // verify a user was created for the username and is associated with the created subscriber
        criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("username", username));
        User user = (User)criteria.uniqueResult();
        assertNotNull(user);
        assertEquals(SecurityUtil.hashPassword(password), user.getPassword());
        assertEquals(subscriber, user.getSubscriber());

        // verify that a conference was created
        Conference conference = new ArrayList<Conference>(user.getConferences()).get(0);
        assertEquals(number, conference.getDescription());
        assertFalse(conference.getIsStarted());

        // verify that the conference has three random pins, one of each type
        Set<Pin> pins = conference.getPins();
        boolean active = false, admin = false, passive = false;
        for(Pin pin : pins)
        {
            switch(pin.getType())
            {
                case ACTIVE:
                    active = true;
                    break;
                case ADMIN:
                    admin = true;
                    break;
                case PASSIVE:
                    passive = true;
                    break;
            }
            assertEquals(10, pin.getNumber().length());
        }

        assertTrue(active);
        assertTrue(admin);
        assertTrue(passive);

        tx.commit();
    }

    @Test
    public void test_doPost_withNoSessionUser_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("user") == null;

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
    public void test_doPost_withoutAdministratorUser_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        setSessionUser(request, false);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
    public void test_doPost_withNullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", (String)null);
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Number", e.getContent());
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
        request.setParameter("number", " ");
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Number", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", (String)null);
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Username", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", " ");
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Username", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Password", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            assertEquals("Please provide a Password", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", (String)null);

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
            assertEquals("Please provide a Confirm Password", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", " ");

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
            assertEquals("Please provide a Confirm Password", e.getContent());
            assertEquals("text/plain", e.getContentType());

        }
        finally
        {
            tx.commit();
        }
    }

    @Test
    public void test_doPost_whenPasswordAndConfirmPasswordDontMatch_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        setSessionUser(request, true);

        request.setMethod("POST");
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password") + "foo");

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
            assertEquals("Password and Confirm Password do not match", e.getContent());
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
