package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class EditSubscriberServletTest extends ListenServletTest
{
    private EditSubscriberServlet servlet = new EditSubscriberServlet();
    private Subscriber subscriber;

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
        // final String username = TestUtil.randomString();
        // final String password = TestUtil.randomString();
        // final String confirm = password;
        final String realName = TestUtil.randomString();
        final String voicemailPin = String.valueOf(subscriber.getId());
        final String enableEmail = "true";
        final String enableSms = "true";
        final String emailAddress = TestUtil.randomString();
        final String smsAddress = TestUtil.randomString();

        request.setMethod("POST");
        request.setParameter("id", id);
        // request.setParameter("username", username);
        // request.setParameter("password", password);
        // request.setParameter("confirmPassword", confirm);
        request.setParameter("realName", realName);
        request.setParameter("voicemailPin", voicemailPin);
        request.setParameter("enableEmail", enableEmail);
        request.setParameter("enableSms", enableSms);
        request.setParameter("emailAddress", emailAddress);
        request.setParameter("smsAddress", smsAddress);
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        // verify a subscriber was modified for the username and is associated with the created subscriber
        Subscriber subscriber = (Subscriber)session.get(Subscriber.class, Long.parseLong(id));
        assertNotNull(subscriber);
        // assertEquals(username, subscriber.getUsername());
        assertEquals(realName, subscriber.getRealName());
    }

    @Test
    public void test_doPost_differentNonAdminWithValidParameters_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        Subscriber subscriber2 = createSubscriber(session);

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

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withNullUsernameAndAdminSubscriber_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", (String)null);
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be null");
    }

    @Test
    public void test_doPost_withBlankUsernameAndAdminSubscriber_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", " ");
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be empty");
    }

    @Test
    public void test_doPost_withNullPasswordIfPasswordConfirmPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", (String)null);
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Password");
    }

    // @Test
    public void test_doPost_withBlankPasswordAndConfirmPasswordPresent_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        subscriber = TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        request.setParameter("id", String.valueOf(subscriber.getId()));
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", " ");
        request.setParameter("confirmPassword", "password");
        request.setParameter("voicemailPin", TestUtil.randomString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Password");
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Confirm Password");
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Confirm Password");
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST,
                                      "Password and Confirm Password do not match");
    }

    @Test
    public void test_doPost_withNullVoicemailPin_isValid() throws ServletException, IOException
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
    public void test_doPost_withBlankVoicemailPin_isValid() throws ServletException, IOException
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Voicemail passcode must contain 0 to 10 numeric characters");
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an Email address");
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an SMS address");
    }

    @Test
    public void test_doPost_adminSubscriberWithEmailAddresNoEnableEmail_editsAccount() throws ServletException,
        IOException
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

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide an SMS address for paging");
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
}
