package com.interact.listen.gui;

import static org.junit.Assert.*;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class AddSubscriberServletTest extends ListenServletTest
{
    private AddSubscriberServlet servlet = new AddSubscriberServlet();

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
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        servlet.service(request, response);

        // verify a subscriber was created for the username and is associated with the created subscriber
        Subscriber subscriber = Subscriber.queryByUsername(session, username);
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
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be null");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Username cannot be empty");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Password cannot be null");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Password cannot be empty");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Confirm Password cannot be null");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Confirm Password cannot be empty");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST,
                                      "Password and Confirm Password do not match");
    }

    @Test
    public void test_doPost_withNullVoicemailPin_isValid() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
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
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
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
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
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
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("enableEmail", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an Email address");
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
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("enableSms", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide an SMS address");
    }

    @Test
    public void test_doPost_adminSubscriberWithEmailAddressNoEnableEmail_editsAccount() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = TestUtil.randomNumeric(4).toString();
        final String enableEmail = "false";
        final String emailAddress = TestUtil.randomString();

        request.setMethod("POST");
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
    public void test_doPost_adminSubscriberWithSmsAddresNoEnableSms_editsAccount() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        final String username = TestUtil.randomString();
        final String password = TestUtil.randomString();
        final String confirm = password;
        final String voicemailPin = TestUtil.randomNumeric(4).toString();
        final String enableSms = "false";
        final String smsAddress = TestUtil.randomString();

        request.setMethod("POST");
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

    @Test
    public void test_doPost_withEnablePagingCheckedAndNoSmsAddress_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("username", TestUtil.randomString());
        request.setParameter("password", TestUtil.randomString());
        request.setParameter("confirmPassword", request.getParameter("password"));
        request.setParameter("voicemailPin", TestUtil.randomNumeric(4).toString());
        request.setParameter("enablePaging", "true");
        request.setParameter("voicemailPlaybackOrder", PlaybackOrder.NEWEST_TO_OLDEST.name());

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide an SMS address for paging");
    }
}
