package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceListServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetConferenceListServlet servlet = new GetConferenceListServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doGet_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        request.setMethod("GET");
        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_UNAUTHORIZED, e.getStatus());
            assertEquals("Unauthorized - Not logged in", e.getContent());
        }
    }

    @Test
    public void test_doGet_returnsListWithNecessaryFields() throws IOException, ServletException
    {
        setSessionSubscriber(request, false);

        request.setMethod("GET");
        servlet.service(request, response);

        assertTrue(request.getOutputBufferString().contains("_fields=description,id,isStarted"));
    }

    // TODO test with administrator subscriber
    // TODO test with non-administrator subscriber

    private void setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setIsAdministrator(isAdministrator);

        HttpSession session = request.getSession();
        session.setAttribute("subscriber", subscriber);
    }
}
