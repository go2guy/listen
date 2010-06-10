package com.interact.listen.gui;

import static org.junit.Assert.*;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.resource.*;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;

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

public class EditUserServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private EditUserServlet servlet;
    private User user;
    private Subscriber subscriber;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new EditUserServlet();
    }

    @Test
    public void test_doPost_adminUserWithValidParameters_editsAccount() throws ServletException, IOException
    {
        setSessionUser(request, true); // admin user
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        session.save(user);

        final String id = String.valueOf(user.getId());
        final String number = randomString();
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("number", number);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);

        servlet.service(request, response);

        // verify a subscriber was created with the provided number since admin is changing the subscriber number
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);

        // verify a user was modified for the username and is associated with the created subscriber
        criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("username", username));
        User user = (User)criteria.uniqueResult();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), user.getPassword());
        assertEquals(subscriber, user.getSubscriber());
        
        session.delete(user);
        session.delete(subscriber);

        tx.commit();
    }
    
    @Test
    public void test_doPost_nonAdminWithValidParameters_editsAccount() throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        final String id = String.valueOf(user.getId());
        final String number = randomString();
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("number", number);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);

        servlet.service(request, response);

        // verify a subscriber was NOT created with the provided number since non-admin is making the edit request
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        Subscriber dbSubscriber = (Subscriber)criteria.uniqueResult();
        assertNull(dbSubscriber);
        assertEquals(user.getSubscriber().getNumber(), subscriber.getNumber());

        // verify a user was modified for the username and is associated with the created subscriber
        criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("username", username));
        User user = (User)criteria.uniqueResult();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), user.getPassword());
        assertEquals(subscriber, user.getSubscriber());
        
        session.delete(user);
        session.delete(subscriber);

        tx.commit();
    }
    
    @Test
    public void test_doPost_differentNonAdminWithValidParameters_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        User user2 = getPopulatedUser();
        
        HttpSession httpSession = request.getSession();
        // Put a different user as the person trying to edit user
        httpSession.setAttribute("user", user2);

        final String id = String.valueOf(user.getId());
        final String number = randomString();
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("number", number);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
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
    public void test_doPost_withNullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", (String)null);
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", " ");
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", (String)null);
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", " ");
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    //@Test
    public void test_doPost_withBlankPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", request.getParameter("password"));

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", (String)null);

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", " ");

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_whenPasswordAndConfirmPasswordDontMatch_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);
        user = getPopulatedUser();
        user.setIsAdministrator(Boolean.FALSE);
        session.save(user);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("user", user);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(user.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password") + "foo");

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
            session.delete(user);
            session.delete(subscriber);
            tx.commit();
        }
    }
    
    private User getPopulatedUser()
    {
        User u = new User();
        u.setId(System.currentTimeMillis());
        u.setSubscriber(subscriber);
        u.setUsername("username");
        u.setPassword("password");
        u.setVersion(1);
        
        return u;
    }
    
    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);
        s.setNumber(String.valueOf(System.currentTimeMillis()));
        s.setVoicemailGreetingLocation("foo/bar/baz/biz");
        
        return s;
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
