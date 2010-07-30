package com.interact.listen.gui;

import static org.junit.Assert.*;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AddSubscriberServletTest extends ListenTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private AddSubscriberServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new AddSubscriberServlet();
    }

    @Test
    public void test_doPost_withValidParameters_provisionsNewAccount() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(TestUtil.randomNumeric(8));
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = TestUtil.randomString();
        final String smsAddress = TestUtil.randomString();
        final String enablePaging = "true";

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("enableSms", enableSms);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("smsAddress", smsAddress);
        request.setParameter("smsPaging", enablePaging);

        servlet.service(request, response);

        // verify a subscriber was created for the username and is associated with the created subscriber
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        Subscriber subscriber = (Subscriber)criteria.uniqueResult();
        assertNotNull(subscriber);
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
        assertEquals(true, subscriber.getIsEmailNotificationEnabled());
        assertEquals(true, subscriber.getIsSmsNotificationEnabled());
        assertEquals(emailAddress, subscriber.getEmailAddress());
        assertEquals(smsAddress, subscriber.getSmsAddress());

        // verify that a conference was created
        Conference conference = new ArrayList<Conference>(subscriber.getConferences()).get(0);
        assertEquals(username + "'s Conference", conference.getDescription());
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
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withoutAdministratorSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withNullUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", (String)null);
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withBlankUsername_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", " ");
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withNullPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withBlankPassword_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withNullConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", (String)null);
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_withBlankConfirmPassword_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", " ");
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }

    @Test
    public void test_doPost_whenPasswordAndConfirmPasswordDontMatch_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password") + "foo");
        request.setParameter("voicemailPin", TestUtil.randomString());

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
    }
    
    @Test
    public void test_doPost_withNullVoicemailPin_isValid()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", (String)null);

        servlet.service(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Test
    public void test_doPost_withBlankVoicemailPin_isValid()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", "");

        servlet.service(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Test
    public void test_doPost_withNonNumericVoicemailPin_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
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
            assertEquals("Voicemail PIN must be a number", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }
    
    @Test
    public void test_doPost_withEnableEmailCheckedAndNoEmailAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
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
    }
    
    @Test
    public void test_doPost_withEnableSmsCheckedAndNoSmsAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
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
    }
    
    @Test
    public void test_doPost_adminSubscriberWithEmailAddressNoEnableEmail_editsAccount() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(TestUtil.randomNumeric(8));
        final String enableEmail = "false";
        final String emailAddress = TestUtil.randomString();

        request.setMethod("POST");
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
    }

    @Test
    public void test_doPost_adminSubscriberWithSmsAddresNoEnableSms_editsAccount() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(TestUtil.randomNumeric(8));
        final String enableSms = "false";
        final String smsAddress = TestUtil.randomString();

        request.setMethod("POST");
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
    }
    
    @Test
    public void test_doPost_withEnablePagingCheckedAndNoSmsAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
        request.setParameter("enablePaging", "true");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("Please provide an SMS address for paging", e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }
}
