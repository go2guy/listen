package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
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

    // TODO re-enable this test once we get the Exception handling filter in place (to handle the
    // ClassNotFoundException thrown from ResourceLocatorFilter)
    // @Test
    // public void test_doGet_nonExistantResource_returns404() throws IOException, ServletException
    // {
    // request.setPathInfo("/FAKE");
    // request.setMethod("GET");
    // request.setQueryString("");
    // servlet.service(request, response);
    //
    // assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    // }

    // POST with incorrect parameters

    @Test
    public void test_doPost_nullAttributeName_throwsServletExceptionWith400BadRequest() throws IOException,
        ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, null);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void test_doPost_contentNotMatchRequestedResource_throwsListenServletExceptionWith400BadRequest()
        throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(subscriber));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void test_doPost_idPresentInHref_throwsListenServletExceptionWith400BadRequest() throws IOException,
        ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, "1");
        request.setMethod("POST");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void test_doPut_idNotPresentInHref_throwsListenServletExceptionWith400BadRequest() throws IOException,
        ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("PUT");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
        }
    }

    // no resource

    @Test
    public void test_doGet_noAttributeName_returns200WithPlainTextMessage() throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, null);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("GET");
        request.setQueryString("");
        servlet.service(request, response);

        final String expectedMessage = "Welcome to the Listen Controller API";
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/plain", request.getOutputBufferType());
        assertEquals(expectedMessage, request.getOutputBufferString());
    }

    @Test
    public void test_doDelete_noAttributeName_throwsListenervletExceptionWith400WithPlainTextMessage()
        throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, null);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("DELETE");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            final String expectedMessage = "Cannot DELETE [null]";
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals(expectedMessage, e.getContent());
        }
    }

    @Test
    public void test_doDelete_noAttributeId_throwsListenServletExceptionWith400WithPlainTextMessage()
        throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("DELETE");

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            final String expectedMessage = "DELETE must be on a specific resource, not the list [Subscriber]";
            assertEquals(HttpServletResponse.SC_BAD_REQUEST, e.getStatus());
            assertEquals("text/plain", e.getContentType());
            assertEquals(expectedMessage, e.getContent());
        }
    }

    // subscribers

    @Test
    public void test_doGet_subscriberNotFound_throwsListenServletExceptionWith404WithNoContent() throws IOException,
        ServletException
    {
        final Long id = System.currentTimeMillis();
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, String.valueOf(id));
        request.setMethod("GET");
        request.setQueryString("");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.getStatus());
            assertEquals("", e.getContent());
        }
        finally
        {
            transaction.commit();
        }
    }

//    @Test
//    public void test_doGet_subscriberWhenNoneExist_returns200WithEmptyXmlList() throws IOException, ServletException
//    {
//        // FIXME this test relied on there being no subscribers in the database, and it shouldn't make that assumption
//        // we should probably delete all subscribers before running this test
//        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
//        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
//        request.setMethod("GET");
//        request.setQueryString("");
//
//        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//        Transaction transaction = session.beginTransaction();
//
//        servlet.service(request, response);
//
//        transaction.commit();
//
//        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
//        expectedXml += "<subscribers href=\"/subscribers?_first=0&amp;_max=100\" count=\"0\" total=\"0\"/>";
//        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
//        assertEquals("application/xml", request.getOutputBufferType());
//        assertEquals(expectedXml, request.getOutputBufferString());
//    }

    @Test
    public void test_doPost_validSubscriber_returns201WithCreatedSubscriberXml() throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(subscriber));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        servlet.service(request, response);

        transaction.commit();

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());
        // TODO assert content
        // TODO delete the subscriber?
    }

    @Test
    public void test_doDelete_validSubscriber_returns204() throws MalformedContentException, IOException,
        ServletException
    {
        // first add a subscriber to delete
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setPassword(String.valueOf(System.currentTimeMillis()));
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        subscriber.setVoicemailPin(System.currentTimeMillis());

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(subscriber));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());

        // get the created subscriber's id
        InputStream is = new ByteArrayInputStream(request.getOutputBufferString().getBytes());
        subscriber = (Subscriber)marshaller.unmarshal(is, new Subscriber(), true);
        Long id = subscriber.getId();

        // now delete it
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();

        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Subscriber.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, String.valueOf(id));
        request.setMethod("DELETE");

        servlet.service(request, response);

        transaction.commit();

        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    // voicemails

    // TODO

    // conferences

    @Test
    public void test_doGet_conferenceNotFound_throwsListenServletExceptionWith404WithNoContent() throws IOException,
        ServletException
    {
        final Long id = System.currentTimeMillis();
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, String.valueOf(id));
        request.setMethod("GET");
        request.setQueryString("");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, e.getStatus());
            assertEquals("", e.getContent());
        }
        finally
        {
            transaction.commit();
        }
    }

    @Test
    public void test_doGet_conferenceWhenNoneExist_returns200WithEmptyXmlList() throws IOException, ServletException
    {
        // FIXME this test relied on there being no conferences in the database, and it shouldn't make that assumption
        // we should probably delete all conferences before running this test
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("GET");
        request.setQueryString("");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        servlet.service(request, response);

        transaction.commit();

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        expectedXml += "<conferences href=\"/conferences?_first=0&amp;_max=100\" count=\"0\" total=\"0\"/>";

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());
        assertEquals(expectedXml, request.getOutputBufferString());
    }

    @Test
    public void test_doPost_validConference_returns201WithCreatedConferenceXml() throws IOException, ServletException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        Conference conference = new Conference();
        conference.setId(System.currentTimeMillis());
        conference.setIsStarted(Boolean.FALSE);
        conference.setIsRecording(Boolean.FALSE);
        conference.setDescription(String.valueOf(System.currentTimeMillis()));

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(conference));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        servlet.service(request, response);

        transaction.commit();

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());
        // TODO assert content
        // TODO delete the conference?
    }

    @Test
    public void test_doPut_validConference_returns200WithCreatedConferenceXml() throws IOException, ServletException,
        MalformedContentException
    {
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, null);
        request.setMethod("POST");

        Conference conference = new Conference();
        conference.setId(System.currentTimeMillis());
        conference.setIsStarted(Boolean.FALSE);
        conference.setIsRecording(Boolean.FALSE);
        conference.setDescription(String.valueOf(System.currentTimeMillis()));

        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.append(marshaller.marshal(conference));

        InputStream stream = new ByteArrayInputStream(content.toString().getBytes());
        DelegatingServletInputStream sstream = new DelegatingServletInputStream(stream);
        request.setInputStream(sstream);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());

        InputStream is = new ByteArrayInputStream(request.getOutputBufferString().getBytes());

        conference = (Conference)marshaller.unmarshal(is, new Conference(), true);

        response = new MockHttpServletResponse();

        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY, Conference.class);
        request.setAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY, String.valueOf(conference.getId()));
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

        transaction.commit();

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("application/xml", request.getOutputBufferType());
        assertTrue(request.getOutputBufferString().contains("<isStarted>true</isStarted>"));
    }

    // participants

    // TODO need to find a way to have resources within a resource (i.e. conference within a participant). Currently
    // doesn't work and is ugly for testing purposes. Works fine in the actual code.
}
