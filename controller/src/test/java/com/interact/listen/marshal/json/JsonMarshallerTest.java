package com.interact.listen.marshal.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.converter.Iso8601DateConverter;
import com.interact.listen.resource.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class JsonMarshallerTest
{
    private JsonMarshaller marshaller;

    @Before
    public void setUp()
    {
        marshaller = new JsonMarshaller();
    }

    @Test
    public void test_marshal_withCompleteVoicemailResource_returnsCorrectJson()
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());

        Voicemail voicemail = new Voicemail();
        voicemail.setDescription(String.valueOf(System.currentTimeMillis()));
        voicemail.setDuration("15:55");
        voicemail.setFileSize("1024");
        voicemail.setForwardedBy(subscriber);
        voicemail.setId(System.currentTimeMillis());
        voicemail.setLeftBy(String.valueOf(System.currentTimeMillis()));
        voicemail.setSubscriber(subscriber);
        voicemail.setUri("/foo/bar/baz/" + System.currentTimeMillis());
        voicemail.setVersion(0);

        SimpleDateFormat sdf = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT);
        String formattedDate = sdf.format(voicemail.getDateCreated());

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"").append("/voicemails/").append(voicemail.getId()).append("\",");
        expected.append("\"dateCreated\":\"").append(formattedDate).append("\",");
        expected.append("\"description\":\"").append(voicemail.getDescription()).append("\",");
        expected.append("\"duration\":\"").append(voicemail.getDuration()).append("\",");
        expected.append("\"fileSize\":\"").append(voicemail.getFileSize()).append("\",");
        expected.append("\"forwardedBy\":{\"href\":\"/subscribers/").append(subscriber.getId()).append("\"},");
        expected.append("\"id\":").append(voicemail.getId()).append(",");
        expected.append("\"isNew\":").append(voicemail.getIsNew()).append(",");
        expected.append("\"leftBy\":\"").append(voicemail.getLeftBy()).append("\",");
        expected.append("\"subscriber\":{\"href\":\"/subscribers/").append(subscriber.getId()).append("\"},");
        expected.append("\"transcription\":null,");
        expected.append("\"uri\":\"").append(voicemail.getUri()).append("\",");
        expected.append("\"version\":").append(voicemail.getVersion());
        expected.append("}");

        assertEquals(expected.toString(), marshaller.marshal(voicemail));
    }

    @Test
    public void test_marshal_withSubscriberList_returnsCorrectJson()
    {
        Subscriber s0 = new Subscriber();
        s0.setId(System.currentTimeMillis());
        Subscriber s1 = new Subscriber();
        s1.setId(System.currentTimeMillis());
        Subscriber s2 = new Subscriber();
        s2.setId(System.currentTimeMillis());
        List<Resource> list = new ArrayList<Resource>(3);
        list.add(s0);
        list.add(s1);
        list.add(s2);

        ResourceList resourceList = new ResourceList();
        resourceList.setList(list);
        resourceList.setFirst(0);
        resourceList.setMax(10);
        resourceList.setTotal(Long.valueOf(3));

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"/subscribers?_first=0&_max=10\",");
        expected.append("\"count\":3,");
        expected.append("\"total\":3,");
        expected.append("\"results\":[");
        expected.append("{\"href\":\"/subscribers/" + s0.getId() + "\"},");
        expected.append("{\"href\":\"/subscribers/" + s1.getId() + "\"},");
        expected.append("{\"href\":\"/subscribers/" + s2.getId() + "\"}");
        expected.append("]");
        expected.append("}");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }

    @Test
    public void test_marshal_withSubscriberListSpecifyingUsernameField_returnsCorrectJsonWithUsername()
    {
        Subscriber s0 = new Subscriber();
        s0.setId(System.currentTimeMillis());
        s0.setUsername("foo" + System.currentTimeMillis());
        Subscriber s1 = new Subscriber();
        s1.setId(System.currentTimeMillis());
        s1.setUsername("foo" + System.currentTimeMillis());
        List<Resource> list = new ArrayList<Resource>(3);
        list.add(s0);
        list.add(s1);

        ResourceList resourceList = new ResourceList();
        resourceList.setList(list);
        resourceList.setFirst(0);
        resourceList.setMax(10);
        resourceList.setTotal(Long.valueOf(2));

        Set<String> fields = new HashSet<String>(1);
        fields.add("username");
        resourceList.setFields(fields);

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"/subscribers?_first=0&_max=10&_fields=username\",");
        expected.append("\"count\":2,");
        expected.append("\"total\":2,");
        expected.append("\"results\":[");
        expected.append("{\"href\":\"/subscribers/" + s0.getId() + "\",\"username\":\"" + s0.getUsername() + "\"},");
        expected.append("{\"href\":\"/subscribers/" + s1.getId() + "\",\"username\":\"" + s1.getUsername() + "\"}");
        expected.append("]");
        expected.append("}");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }

    @Test
    public void test_marshal_withSubscriberListAndPagedResults_returnsCorrectJsonWithPaging()
    {
        Subscriber s0 = new Subscriber();
        s0.setId(System.currentTimeMillis());
        Subscriber s1 = new Subscriber();
        s1.setId(System.currentTimeMillis());
        Subscriber s2 = new Subscriber();
        s2.setId(System.currentTimeMillis());
        List<Resource> list = new ArrayList<Resource>(3);
        list.add(s0);
        list.add(s1);
        list.add(s2);

        ResourceList resourceList = new ResourceList();
        resourceList.setList(list);
        resourceList.setFirst(0);
        resourceList.setMax(3);
        resourceList.setTotal(Long.valueOf(5));

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"/subscribers?_first=0&_max=3\",");
        expected.append("\"count\":3,");
        expected.append("\"total\":5,");
        expected.append("\"next\":\"/subscribers?_first=3&_max=3\",");
        expected.append("\"results\":[");
        expected.append("{\"href\":\"/subscribers/" + s0.getId() + "\"},");
        expected.append("{\"href\":\"/subscribers/" + s1.getId() + "\"},");
        expected.append("{\"href\":\"/subscribers/" + s2.getId() + "\"}");
        expected.append("]");
        expected.append("}");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }

    @Test
    public void test_marshal_withNullList_throwsExceptionWithMessage()
    {
        try
        {
            marshaller.marshal(null, Subscriber.class);
            fail("Expected IllegalArgumentException for marshal() with null list");
        }
        catch(IllegalArgumentException e)
        {
            assertEquals("List cannot be null", e.getMessage());
        }
    }

    @Test
    public void test_unmarshal_withUnmarshalIdFalse_unmarshalsSubscriberWithoutId() throws MalformedContentException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setUsername("foo" + System.currentTimeMillis());
        subscriber.setVoicemailPin(System.currentTimeMillis());
        subscriber.setLastLogin(new Date());

        String json = marshaller.marshal(subscriber);
        InputStream stream = new ByteArrayInputStream(json.getBytes());

        Subscriber unmarshalledSubscriber = (Subscriber)marshaller.unmarshal(stream, new Subscriber(), false);

        assertNull(unmarshalledSubscriber.getId());
        assertEquals(subscriber.getUsername(), unmarshalledSubscriber.getUsername());
    }

    @Test
    public void test_unmarshal_withUnmarshalIdTrue_unmarshalsSubscriberWithId() throws MalformedContentException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setUsername("foo" + System.currentTimeMillis());
        subscriber.setVoicemailPin(System.currentTimeMillis());
        subscriber.setLastLogin(new Date());

        String json = marshaller.marshal(subscriber);
        InputStream stream = new ByteArrayInputStream(json.getBytes());

        Subscriber unmarshalledSubscriber = (Subscriber)marshaller.unmarshal(stream, new Subscriber(), true);

        assertEquals(subscriber.getId(), unmarshalledSubscriber.getId());
        assertEquals(subscriber.getUsername(), unmarshalledSubscriber.getUsername());
    }

    @Test
    public void test_getContentType_returnsApplicationJson()
    {
        assertEquals("application/json", marshaller.getContentType());
    }

    @Test
    public void test_escape_withQuoteInValue_escapesQuote()
    {
        final String value = "fo\"o";
        assertEquals("fo\\\"o", marshaller.escape(value));
    }

    @Test
    public void test_escape_withNullValue_returnsNull()
    {
        assertNull(marshaller.escape(null));
    }

    @Test
    public void test_escape_withNonQuotedValue_returnsValue()
    {
        final String value = "foo";
        assertEquals(value, marshaller.escape(value));
    }
}
