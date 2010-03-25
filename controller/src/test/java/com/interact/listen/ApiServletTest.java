package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.*;

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
    private XmlMarshaller marshaller = new XmlMarshaller();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    // non-existant resource type
    
    @Test
    public void test_doGet_nonExistantResource_returns404() throws IOException, ServletException
    {
        request.setPathInfo("/FAKE");
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
    
    // POST with incorrect parameters
    
    @Test
    public void test_doPost_nullAttributeName_returns400BadRequest() throws IOException, ServletException
    {
        request.setPathInfo("/");
        request.setMethod("POST");

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test
    public void test_doPost_sendId_returns400BadRequest() throws IOException, ServletException
    {
        request.setPathInfo("/subscribers/" + System.currentTimeMillis());
        request.setMethod("POST");

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Test
    public void test_doPost_contentNotMatchRequestedResource_returns400BadRequest() throws IOException, ServletException
    {
        request.setPathInfo("/conferences");
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
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
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

    @Test
    public void test_doDelete_anyResource_returns405WithPlainTextMessage() throws IOException, ServletException
    {
        request.setPathInfo("/subscriber");
        request.setMethod("DELETE");
        servlet.service(request, response);

        final String expectedMessage = "DELETE requests are not allowed";
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
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

    @Test
    public void test_doGet_conferenceNotFound_returns404WithNoContent() throws IOException, ServletException
    {
        final Long id = System.currentTimeMillis();
        request.setPathInfo("/conferences/" + id);
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        assertEquals("", response.getContentAsString());
    }

    @Test
    public void test_doGet_conferenceWhenNoneExist_returns200WithEmptyXmlList() throws IOException, ServletException
    {
        // FIXME this test relied on there being no conferences in the database, and it shouldn't make that assumption
        // we should probably delete all conferences before running this test
        request.setPathInfo("/conferences");
        request.setMethod("GET");
        servlet.service(request, response);

        final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><conferences href=\"/conferences\"/>";
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertEquals(expectedXml, response.getContentAsString());
    }

    @Test
    public void test_doPost_validConference_returns201WithCreatedConferenceXml() throws IOException, ServletException
    {
        request.setPathInfo("/conferences");
        request.setMethod("POST");

        Conference conference = new Conference();
        conference.setId(System.currentTimeMillis());
        conference.setNumber(String.valueOf(System.currentTimeMillis()));
        conference.setAdminPin(String.valueOf(System.currentTimeMillis()));
        conference.setIsStarted(Boolean.FALSE);

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(conference));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        // TODO assert content
        // TODO delete the conference?
    }
    
    @Test
    public void test_doPut_validConference_returns200WithCreatedConferenceXml() throws IOException, ServletException,
        MalformedContentException
    {
        request.setPathInfo("/conferences");
        request.setMethod("POST");

        Conference conference = new Conference();
        conference.setId(System.currentTimeMillis());
        conference.setNumber(String.valueOf(System.currentTimeMillis()));
        conference.setAdminPin(String.valueOf(System.currentTimeMillis()));
        conference.setIsStarted(Boolean.FALSE);

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(conference));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        
        conference = (Conference)marshaller.unmarshal(new ByteArrayInputStream(
                                                      response.getContentAsString().getBytes()), Conference.class);
        
        response = new MockHttpServletResponse();
                
        request.setPathInfo("/conferences/" + conference.getId());
        request.setMethod("PUT");
        
        // Change one attribute of the conference
        conference.setIsStarted(Boolean.TRUE);
        
        content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(conference));

        stream = new ByteArrayInputStream(content.toString().getBytes());
        sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        servlet.service(request, response);
        
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        assertTrue(response.getContentAsString().contains("<isStarted>true</isStarted>"));
    }

    // participants

    @Test
    public void test_doPost_validParticipant_returns201WithCreatedParticipantXml() throws IOException, ServletException
    {
        request.setPathInfo("/participants");
        request.setMethod("POST");

        Participant participant = new Participant();
        participant.setId(System.currentTimeMillis());
        participant.setNumber(String.valueOf(System.currentTimeMillis()));
        participant.setConference(null);
        participant.setIsAdmin(Boolean.FALSE);
        participant.setIsHolding(Boolean.FALSE);
        participant.setIsMuted(Boolean.FALSE);
        participant.setAudioResource("/foo/bar/baz.wav");
        participant.setSessionID(String.valueOf(System.currentTimeMillis()));

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(participant));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", response.getContentType());
        // TODO assert content
        // TODO delete the participant?
    }
}
