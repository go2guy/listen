package com.interact.listen;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiServletTest
{
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    HttpServlet servlet = new ApiServlet();

    @Before
    public void setUp()
    {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_noAttributeName_returns200WithPlainTextMessage() throws IOException, ServletException
    {
        request.setPathInfo("/");
        request.setMethod("GET");
        servlet.service(request, response);

        final String expectedMessage = "Welcome to the Listen Controller API";
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals(expectedMessage, response.getContentAsString());
    }

    @Test
    public void test_doGet_subscriberNotFound_returns404WithNoContent() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();
        request.setPathInfo("/subscribers/" + id);
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    public void test_doGet_subscriberWhenNoneExist_returns200WithEmptyXmlList() throws IOException, ServletException
    {
        request.setPathInfo("/subscribers");
        request.setMethod("GET");
        servlet.service(request, response);

        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><subscribers href=\"/subscribers\"/>";
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertEquals(expectedXml, response.getContentAsString());
    }
}
