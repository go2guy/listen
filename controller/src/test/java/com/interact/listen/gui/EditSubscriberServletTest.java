package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.resource.Subscriber;
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

public class EditSubscriberServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private EditSubscriberServlet servlet;
    private Subscriber subscriber;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new EditSubscriberServlet();
    }

    @Test
    public void test_doPost_adminSubscriberWithValidParameters_editsAccount() throws ServletException, IOException
    {
        setSessionSubscriber(request, true); // admin subscriber
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        session.save(subscriber);

        final String id = String.valueOf(subscriber.getId());
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

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());

        session.delete(subscriber);
        tx.commit();
    }
    
    @Test
    public void test_doPost_nonAdminWithValidParameters_editsAccount() throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        final String id = String.valueOf(subscriber.getId());
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

        // verify a subscriber was modified for the username and is associated with the created subscriber
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());

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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        Subscriber subscriber2 = getPopulatedSubscriber();

        HttpSession httpSession = request.getSession();
        // Put a different subscriber as the person trying to edit subscriber
        httpSession.setAttribute("subscriber", subscriber2);

        final String id = String.valueOf(subscriber.getId());
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
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullPasswordIfPasswordConfirmPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", "password");

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
            session.delete(subscriber);
            tx.commit();
        }
    }

    //@Test
    public void test_doPost_withBlankPasswordAndConfirmPasswordPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("number", randomString());
        request.setParameter("username", randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", "password");

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
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withNullConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
            session.delete(subscriber);
            tx.commit();
        }
    }

    @Test
    public void test_doPost_withBlankConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = getPopulatedSubscriber();
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
        subscriber.setIsAdministrator(Boolean.FALSE);
        session.save(subscriber);
        
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
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
            session.delete(subscriber);
            tx.commit();
        }
    }

    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setNumber(String.valueOf(System.currentTimeMillis()));
        s.setPassword("password");
        s.setUsername("username");
        s.setVersion(1);
        s.setVoicemailGreetingLocation("foo/bar/baz/biz");
        return s;
    }

    // TODO this is used in several servlets - refactor it into some test utility class
    private void setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        subscriber.setIsAdministrator(isAdministrator);

        HttpSession session = request.getSession();
        session.setAttribute("subscriber", subscriber);
    }

    // TODO refactor this out into test utils
    private String randomString()
    {
        return String.valueOf(System.currentTimeMillis());
    }
}
