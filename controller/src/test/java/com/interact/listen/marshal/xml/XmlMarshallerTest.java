package com.interact.listen.marshal.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.converter.Iso8601DateConverter;
import com.interact.listen.resource.*;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

public class XmlMarshallerTest
{
    private XmlMarshaller marshaller;

    @Before
    public void setUp()
    {
        marshaller = new XmlMarshaller();
    }

    @Test
    public void test_marshal_withCompleteVoicemailResource_returnsCorrectXml()
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setNumber("foo" + System.currentTimeMillis());

        Voicemail voicemail = new Voicemail();
        voicemail.setDescription(String.valueOf(System.currentTimeMillis()));
        voicemail.setDuration("15:55");
        voicemail.setFileSize("1024");
        voicemail.setId(System.currentTimeMillis());
        voicemail.setLeftBy(String.valueOf(System.currentTimeMillis()));
        voicemail.setSubscriber(subscriber);
        voicemail.setUri("/foo/bar/baz/" + System.currentTimeMillis());
        voicemail.setVersion(0);

        SimpleDateFormat sdf = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT);
        String formattedDate = sdf.format(voicemail.getDateCreated());

        StringBuilder expected = new StringBuilder();
        expected.append("<voicemail href=\"/voicemails/").append(voicemail.getId()).append("\">");
        expected.append("<dateCreated>").append(formattedDate).append("</dateCreated>");
        expected.append("<description>").append(voicemail.getDescription()).append("</description>");
        expected.append("<duration>").append(voicemail.getDuration()).append("</duration>");
        expected.append("<fileSize>").append(voicemail.getFileSize()).append("</fileSize>");
        expected.append("<id>").append(voicemail.getId()).append("</id>");
        expected.append("<isNew>").append(voicemail.getIsNew()).append("</isNew>");
        expected.append("<leftBy>").append(voicemail.getLeftBy()).append("</leftBy>");
        expected.append("<subscriber href=\"/subscribers/").append(subscriber.getId()).append("\"/>");
        expected.append("<uri>").append(voicemail.getUri()).append("</uri>");
        expected.append("<version>").append(voicemail.getVersion()).append("</version>");
        expected.append("</voicemail>");

        assertEquals(expected.toString(), marshaller.marshal(voicemail));
    }

    @Test
    public void test_marshal_withSubscriberList_returnsCorrectXml()
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
        resourceList.setMax(10);
        resourceList.setFirst(0);
        resourceList.setTotal(Long.valueOf(3));

        StringBuilder expected = new StringBuilder();
        expected.append("<subscribers href=\"/subscribers?_first=0&amp;_max=10\" count=\"3\" total=\"3\">");
        expected.append("<subscriber href=\"/subscribers/").append(s0.getId()).append("\"/>");
        expected.append("<subscriber href=\"/subscribers/").append(s1.getId()).append("\"/>");
        expected.append("<subscriber href=\"/subscribers/").append(s2.getId()).append("\"/>");
        expected.append("</subscribers>");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }
    
    @Test
    public void test_marshal_withSubscriberListSpecifyingNumberField_returnsListWithNumberAttribute()
    {
        Subscriber s0 = new Subscriber();
        s0.setId(System.currentTimeMillis());
        s0.setNumber("foo" + System.currentTimeMillis());
        Subscriber s1 = new Subscriber();
        s1.setId(System.currentTimeMillis());
        s1.setNumber("foo" + System.currentTimeMillis());
        List<Resource> list = new ArrayList<Resource>(3);
        list.add(s0);
        list.add(s1);

        ResourceList resourceList = new ResourceList();
        resourceList.setList(list);
        resourceList.setMax(10);
        resourceList.setFirst(0);
        resourceList.setTotal(Long.valueOf(2));
        
        Set<String> fields = new HashSet<String>(1);
        fields.add("number");
        resourceList.setFields(fields);

        StringBuilder expected = new StringBuilder();
        expected.append("<subscribers href=\"/subscribers?_first=0&amp;_max=10&amp;_fields=number\" count=\"2\" total=\"2\">");
        expected.append("<subscriber href=\"/subscribers/").append(s0.getId()).append("\" number=\"").append(s0.getNumber()).append("\"/>");
        expected.append("<subscriber href=\"/subscribers/").append(s1.getId()).append("\" number=\"").append(s1.getNumber()).append("\"/>");
        expected.append("</subscribers>");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }
    
    @Test
    public void test_marshal_withSubscriberListAndPagedResults_returnsCorrectXmlWithPaging()
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
        resourceList.setMax(3);
        resourceList.setFirst(0);
        resourceList.setTotal(Long.valueOf(5));

        StringBuilder expected = new StringBuilder();
        expected.append("<subscribers href=\"/subscribers?_first=0&amp;_max=3\" count=\"3\" total=\"5\" next=\"/subscribers?_first=3&amp;_max=3\">");
        expected.append("<subscriber href=\"/subscribers/").append(s0.getId()).append("\"/>");
        expected.append("<subscriber href=\"/subscribers/").append(s1.getId()).append("\"/>");
        expected.append("<subscriber href=\"/subscribers/").append(s2.getId()).append("\"/>");
        expected.append("</subscribers>");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }
    
    @Test
    public void test_marshal_withNullList_throwsIllegalArgumentExceptionWithMessage()
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
        subscriber.setNumber("foo" + System.currentTimeMillis());

        String xml = marshaller.marshal(subscriber);
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

        Subscriber unmarshalledSubscriber = (Subscriber)marshaller.unmarshal(stream, new Subscriber(), false);

        assertNull(unmarshalledSubscriber.getId());
        assertEquals(subscriber.getNumber(), unmarshalledSubscriber.getNumber());
    }

    @Test
    public void test_unmarshal_withUnmarshalIdTrue_unmarshalsSubscriberWithId() throws MalformedContentException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setNumber("foo" + System.currentTimeMillis());

        String xml = marshaller.marshal(subscriber);
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

        Subscriber unmarshalledSubscriber = (Subscriber)marshaller.unmarshal(stream, new Subscriber(), true);

        assertEquals(subscriber.getId(), unmarshalledSubscriber.getId());
        assertEquals(subscriber.getNumber(), unmarshalledSubscriber.getNumber());
    }

    @Test
    public void test_getContentType_returnsApplicationXml()
    {
        assertEquals("application/xml", marshaller.getContentType());
    }

    @Test
    public void test_escape_withLeftAngleBracket_escapesLeftAngleBracket()
    {
        final String value = "fo<o";
        assertEquals("fo&lt;o", marshaller.escape(value));
    }

    @Test
    public void test_escape_withRightAngleBracket_escapesRightAngleBracket()
    {
        final String value = "fo>o";
        assertEquals("fo&gt;o", marshaller.escape(value));
    }

    @Test
    public void test_escape_withAmpersand_escapesAmpersand()
    {
        final String value = "fo&o";
        assertEquals("fo&amp;o", marshaller.escape(value));
    }

    @Test
    public void test_escape_withNullValue_returnsNull()
    {
        assertNull(marshaller.escape(null));
    }

    @Test
    public void test_escape_withValueNotNeedingEscaping_returnsValue()
    {
        final String value = "foo";
        assertEquals(value, marshaller.escape(value));
    }
}
