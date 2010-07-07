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
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, true); // admin subscriber

        final String id = String.valueOf(subscriber.getId());
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = randomString();
        final String smsAddress = randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("enableSms", enableSms);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("smsAddress", smsAddress);

        servlet.service(request, response);

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("id", Long.valueOf(id)));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());

        tx.commit();
    }
    
    @Test
    public void test_doPost_nonAdminWithValidParameters_editsAccount() throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = randomString();
        final String smsAddress = randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("enableSms", enableSms);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("smsAddress", smsAddress);

        servlet.service(request, response);

        // verify a subscriber was modified for the username and is associated with the created subscriber
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());

        tx.commit();
    }
    
    @Test
    public void test_doPost_differentNonAdminWithValidParameters_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);
        
        Subscriber subscriber2 = getPopulatedSubscriber();
        session.save(subscriber2);

        // We want the subscriber being edited to be subscriber 2 being edited by subscriber 1 who is not an admin
        final String id = String.valueOf(subscriber2.getId());
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber2.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = randomString();
        final String smsAddress = randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("enableSms", enableSms);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("smsAddress", smsAddress);

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
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", randomString());

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
    public void test_doPost_withNullUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", (String)null);
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", randomString());

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
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", " ");
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", randomString());

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
    public void test_doPost_withNullPasswordIfPasswordConfirmPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", randomString());

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

    //@Test
    public void test_doPost_withBlankPasswordAndConfirmPasswordPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", randomString());

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
    public void test_doPost_withNullConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", (String)null);
        request.setParameter("voicemailPin", randomString());

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
    public void test_doPost_withBlankConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", " ");
        request.setParameter("voicemailPin", randomString());

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
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password") + "foo");
        request.setParameter("voicemailPin", randomString());

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
    
    @Test
    public void test_doPost_withNullVoicemailPin_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", (String)null);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a Voicemail Pin Number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_withBlankVoicemailPin_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", "");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide a Voicemail Pin Number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_withNonNumericVoicemailPin_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", "ABC");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Voicemail Pin Number can only be digits 0-9", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_withEnableEmailCheckedAndNoEmailAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", randomString().substring(0,10));
        request.setParameter("enableEmail", "true");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide an E-mail address", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_withEnableSmsCheckedAndNoSmsAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, false);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", randomString());
        request.setParameter("password", randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", randomString().substring(0,10));
        request.setParameter("enableSms", "true");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide an SMS address", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
        finally
        {
            tx.commit();
        }
    }
    
    @Test
    public void test_doPost_adminSubscriberWithEmailAddresNoEnableEmail_editsAccount() throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, true);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "false";
        final String emailAddress = randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("emailAddress", emailAddress);

        servlet.service(request, response);

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
        assertEquals(emailAddress, subscriber.getEmailAddress());

        tx.commit();
    }
    
    @Test
    public void test_doPost_adminSubscriberWithSmsAddresNoEnableSms_editsAccount() throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        
        subscriber = setSessionSubscriber(request, true);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = randomString();
        final String password = randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableSms = "false";
        final String smsAddress = randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableSms", enableSms);
        request.setParameter("smsAddress", smsAddress);

        servlet.service(request, response);

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
        assertEquals(smsAddress, subscriber.getSmsAddress());

        tx.commit();
    }

    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setPassword("password");
        s.setUsername("username");
        s.setVoicemailPin(System.currentTimeMillis());
        s.setVersion(1);
        return s;
    }

    // TODO this is used in several servlets - refactor it into some test utility class
    private Subscriber setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
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
        
        return subscriber;
    }

    // TODO refactor this out into test utils
    private String randomString()
    {
        return String.valueOf(System.currentTimeMillis());
    }
}
