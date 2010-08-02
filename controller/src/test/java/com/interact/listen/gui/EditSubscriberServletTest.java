package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EditSubscriberServletTest extends ListenTest
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
        subscriber = TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String id = String.valueOf(subscriber.getId());
        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = TestUtil.randomString();
        final String smsAddress = TestUtil.randomString();
        final String enablePaging = "true";

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
        request.setParameter("enablePaging", enablePaging);
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        Subscriber subscriber = Subscriber.queryById(session, Long.parseLong(id));
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
    }
    
    @Test
    public void test_doPost_nonAdminWithValidParameters_editsAccount() throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = TestUtil.randomString();
        final String smsAddress = TestUtil.randomString();

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
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        // verify a subscriber was modified for the username and is associated with the created subscriber
        Subscriber subscriber = Subscriber.queryByUsername(session, username);
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
    }
    
    @Test
    public void test_doPost_differentNonAdminWithValidParameters_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);
        
        Subscriber subscriber2 = getPopulatedSubscriber();
        session.save(subscriber2);

        // We want the subscriber being edited to be subscriber 2 being edited by subscriber 1 who is not an admin
        final String id = String.valueOf(subscriber2.getId());
        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber2.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = TestUtil.randomString();
        final String smsAddress = TestUtil.randomString();

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
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws ServletException,
        IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", (String)null);
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", " ");
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    public void test_doPost_withNullPasswordIfPasswordConfirmPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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

    //@Test
    public void test_doPost_withBlankPasswordAndConfirmPasswordPresent_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    public void test_doPost_withNullConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", (String)null);
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    public void test_doPost_withBlankConfirmPasswordWhenPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", " ");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password") + "foo");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", (String)null);
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Test
    public void test_doPost_withBlankVoicemailPin_isValid()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", "");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Test
    public void test_doPost_withNonNumericVoicemailPin_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", "ABC");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
        request.setParameter("enableEmail", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
        request.setParameter("enableSms", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    public void test_doPost_adminSubscriberWithEmailAddresNoEnableEmail_editsAccount() throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, true, session);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "false";
        final String emailAddress = TestUtil.randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        Subscriber subscriber = Subscriber.queryByUsername(session, username);
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
        assertEquals(emailAddress, subscriber.getEmailAddress());
    }
    
    @Test
    public void test_doPost_withEnablePagingCheckedAndNoSmsAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", String.valueOf(TestUtil.randomNumeric(8)));
        request.setParameter("enablePaging", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

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
    
    @Test
    public void test_doPost_adminSubscriberWithSmsAddresNoEnableSms_editsAccount() throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, true, session);
        
        final String id = String.valueOf(subscriber.getId());
        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableSms = "false";
        final String smsAddress = TestUtil.randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        request.setParameter("username", username);
        request.setParameter("password", password);
        request.setParameter("confirmPassword", confirm);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableSms", enableSms);
        request.setParameter("smsAddress", smsAddress);
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        Subscriber subscriber = Subscriber.queryByUsername(session, username);
        assertNotNull(subscriber);
        assertEquals(username, subscriber.getUsername());
        assertEquals(SecurityUtil.hashPassword(password), subscriber.getPassword());
        assertEquals(smsAddress, subscriber.getSmsAddress());
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
}
