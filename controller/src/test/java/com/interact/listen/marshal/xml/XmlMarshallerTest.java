package com.interact.listen.marshal.xml;

import static org.junit.Assert.assertEquals;

import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.converter.Iso8601DateConverter;
import com.interact.listen.resource.*;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
        voicemail.setId(System.currentTimeMillis());
        voicemail.setSubscriber(subscriber);
        voicemail.setFileLocation("/foo/bar/baz/" + System.currentTimeMillis());
        voicemail.setVersion(0);

        SimpleDateFormat sdf = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT);
        String formattedDate = sdf.format(voicemail.getDateCreated());

        StringBuilder expected = new StringBuilder();
        expected.append("<voicemail href=\"/voicemails/").append(voicemail.getId()).append("\">");
        expected.append("<dateCreated>").append(formattedDate).append("</dateCreated>");
        expected.append("<fileLocation>").append(voicemail.getFileLocation()).append("</fileLocation>");
        expected.append("<id>").append(voicemail.getId()).append("</id>");
        expected.append("<isNew>").append(voicemail.getIsNew()).append("</isNew>");
        expected.append("<subscriber href=\"/subscribers/").append(subscriber.getId()).append("\"/>");
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
    public void test_unmarshal() throws MalformedContentException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setNumber("foo" + System.currentTimeMillis());

        String xml = marshaller.marshal(subscriber);
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

        Subscriber unmarshalledSubscriber = (Subscriber)marshaller.unmarshal(stream, Subscriber.class);

        assertEquals(subscriber.getId(), unmarshalledSubscriber.getId());
        assertEquals(subscriber.getNumber(), unmarshalledSubscriber.getNumber());
    }
}
