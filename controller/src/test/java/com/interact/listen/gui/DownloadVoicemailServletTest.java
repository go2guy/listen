package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class DownloadVoicemailServletTest extends ListenServletTest
{
    private DownloadVoicemailServlet servlet = new DownloadVoicemailServlet();

    @Test
    public void test_doGet_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withNullIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("GET");
        request.setParameter("id", (String)null);
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doGet_withBlankIdParameter_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("GET");
        request.setParameter("id", " ");
        testForListenServletException(servlet, 400, "Please provide an id");
    }

    @Test
    public void test_doGet_withNonAdminSubscriberWhoDoesntOwnVoicemail_throwsUnauthorized() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        Subscriber subscriber = createSubscriber(session);
        Voicemail voicemail = createVoicemail(session, subscriber);

        assert subscriber != ServletUtil.currentSubscriber(request);

        request.setMethod("GET");
        request.setParameter("id", String.valueOf(voicemail.getId()));
        testForListenServletException(servlet, 401, "Unauthorized - Not allowed to download voicemail");
    }

    // TODO test with admin subscriber and voicemail not owned by them
    // TODO test with non-admin subscriber and voicemail owned by them
}
