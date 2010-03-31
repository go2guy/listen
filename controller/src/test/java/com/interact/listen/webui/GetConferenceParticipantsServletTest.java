package com.interact.listen.webui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceParticipantsServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet = new GetConferenceParticipantsServlet();

    // TODO do we need to return something different if the conference itself is not found? 404?

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_withNonexistentConference_returnsEmptyResultAnd200Response() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();

        request.setMethod("GET");
        request.setParameter("conference", String.valueOf(id));
        servlet.service(request, response);

        final String expectedBody = "{\"href\":\"/participants?_first=0&_max=100&_fields=isHolding,isAdmin,number,isMuted&conference=/conferences/" + id + "\",\"count\":0,\"total\":0,\"results\":[]}";

        assertEquals(expectedBody, response.getContentAsString());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
