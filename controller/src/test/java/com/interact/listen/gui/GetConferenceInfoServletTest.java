package com.interact.listen.gui;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

public class GetConferenceInfoServletTest extends ListenServletTest
{
    private GetConferenceInfoServlet servlet = new GetConferenceInfoServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doGet_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        request.setMethod("GET");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withNonexistentConference_throwsListenServletExceptionWith500() throws IOException,
        ServletException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("GET");

        testForListenServletException(servlet, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Conference not found");
    }

    @Test
    public void test_doGet_withExistingConference_returns200AndConferenceJSON() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        Conference conference = new Conference();
        conference.setIsStarted(true);
        conference.setIsRecording(false);
        conference.setId(System.currentTimeMillis());
        conference.setDescription(String.valueOf(System.currentTimeMillis()));
        session.save(conference);

        Subscriber subscriber = new Subscriber();
        subscriber.setId(id);
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        subscriber.addToConferences(conference);
        session.save(subscriber);

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("subscriber", subscriber.getId());

        request.setMethod("GET");
        servlet.service(request, response);

        String hrefString = "\"href\":\"/conferences/" + conference.getId() + "\"";
        assertTrue(request.getOutputBufferString().contains(hrefString));
    }

    @Test(expected = ListenServletException.class)
    public void test_doGet_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("GET");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_GET_CONFERENCE_INFO);
    }
}
