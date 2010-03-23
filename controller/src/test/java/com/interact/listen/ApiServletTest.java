package com.interact.listen;

import static org.junit.Assert.assertEquals;

import com.interact.listen.resource.Subscriber;
import com.interact.listen.xml.Marshaller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockHttpServletResponse;

public class ApiServletTest
{
    private InputStreamMockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet = new ApiServlet();
    private Marshaller marshaller = new Marshaller();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    // no resource

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

    // subscribers

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
        // FIXME this test relied on there being no subscribers in the database, and it shouldn't make that assumption
        // we should probably delete all subscribers before running this test
        request.setPathInfo("/subscribers");
        request.setMethod("GET");
        servlet.service(request, response);

        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><subscribers href=\"/subscribers\"/>";
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertEquals(expectedXml, response.getContentAsString());
    }

    @Test
    public void test_doPost_validSubscriber_returns201WithCreatedSubscriberXml() throws IOException, ServletException
    {
        request.setPathInfo("/subscribers");
        request.setMethod("POST");

        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(subscriber));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        // TODO assert content
        // TODO delete the subscriber?
    }

    // voicemails

    // TODO

    // conferences

    // TODO

    // participants

    // TODO
}
