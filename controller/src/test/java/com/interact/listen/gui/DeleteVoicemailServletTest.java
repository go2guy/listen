package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class DeleteVoicemailServletTest extends ListenServletTest
{
    private DeleteVoicemailServlet servlet = new DeleteVoicemailServlet();

    @Test
    public void test_doPost_withValidVoicemailIdAndSubscriberOwnsVoicemail_deletesVoicemail() throws ServletException,
        IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, false, session);
        Voicemail voicemail = createAndDeleteVoicemail(subscriber);
        assertNull(Voicemail.queryById(session, voicemail.getId()));
    }

    @Test
    public void test_doPost_withValidVoicemailIdAndCurrentUserAdministrator_deletesVoicemail() throws ServletException,
        IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, true, session);
        Voicemail voicemail = createAndDeleteVoicemail(subscriber);
        assertNull(Voicemail.queryById(session, voicemail.getId()));
    }

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsException() throws ServletException, IOException
    {
        Subscriber subscriber = createSubscriber(session);
        Voicemail voicemail = createVoicemail(session, subscriber);

        try
        {
            doPost(voicemail.getId());
            fail("Expected UnauthorizedServletException");
        }
        catch(UnauthorizedServletException e)
        {
            assertEquals("Unauthorized - Not logged in", e.getContent());
        }
    }

    @Test
    public void test_doPost_whenVoicemailDeleted_sendsStat() throws ServletException, IOException
    {
        StatSender sender = setupMockStatSender(request);

        Subscriber subscriber = TestUtil.setSessionSubscriber(request, false, session);
        createAndDeleteVoicemail(subscriber);

        verify(sender).send(Stat.GUI_DELETE_VOICEMAIL);
    }

    // TODO
    public void test_doPost_whenVoicemailDeleted_writesHistory()
    {}

    @Test
    public void test_doPost_missingIdParameter_throwsException() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        try
        {
            doPost(null);
            fail("Expected BadRequestServletException");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Please provide an id", e.getContent());
        }
    }

    @Test
    public void test_doPost_voicemailNotFound_throwsException() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        Long id = getUniqueVoicemailId();
        try
        {
            doPost(id);
            fail("Expected BadRequestServletException");
        }
        catch(BadRequestServletException e)
        {
            assertEquals("Voicemail not found", e.getContent());
        }
    }

    private Long getUniqueVoicemailId()
    {
        Long id;
        do
        {
            id = TestUtil.randomNumeric(12);
        }
        while(Voicemail.queryById(session, id) != null);
        return id;
    }

    @Test
    public void test_doPost_validVoicemailIdButNotOwnedByCurrentSubscriber_throwsException() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        Subscriber subscriber = createSubscriber(session);

        try
        {
            createAndDeleteVoicemail(subscriber);
            fail("Expected UnauthorizedServletException");
        }
        catch(UnauthorizedServletException e)
        {
            assertEquals("Unauthorized - Subscriber does not own Voicemail", e.getContent());
        }
    }

    private Voicemail createAndDeleteVoicemail(Subscriber subscriberOwningVoicemail) throws ServletException,
        IOException
    {
        Voicemail voicemail = createVoicemail(session, subscriberOwningVoicemail);
        doPost(voicemail.getId());
        return voicemail;
    }

    private void doPost(Long voicemailId) throws ServletException, IOException
    {
        request.setMethod("POST");
        if(voicemailId != null)
        {
            request.setParameter("id", String.valueOf(voicemailId));
        }
        servlet.service(request, response);
    }

    private static StatSender setupMockStatSender(HttpServletRequest request)
    {
        StatSender sender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", sender);
        return sender;
    }
}
