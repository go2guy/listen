package com.interact.listen.marshal.json;

import static org.junit.Assert.assertEquals;

import com.interact.listen.marshal.converter.Iso8601DateConverter;
import com.interact.listen.resource.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
        subscriber.setNumber("foo" + System.currentTimeMillis());

        Voicemail voicemail = new Voicemail();
        voicemail.setId(System.currentTimeMillis());
        voicemail.setSubscriber(subscriber);
        voicemail.setFileLocation("/foo/bar/baz/" + System.currentTimeMillis());
        voicemail.setVersion(0);

        SimpleDateFormat sdf = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT);
        String formattedDate = sdf.format(voicemail.getDateCreated());

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"").append("/voicemails/").append(voicemail.getId()).append("\",");
        expected.append("\"dateCreated\":\"").append(formattedDate).append("\",");
        expected.append("\"fileLocation\":\"").append(voicemail.getFileLocation()).append("\",");
        expected.append("\"id\":").append(voicemail.getId()).append(",");
        expected.append("\"isNew\":").append(voicemail.getIsNew()).append(",");
        expected.append("\"subscriber\":{\"href\":\"/subscribers/").append(subscriber.getId()).append("\"},");
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

        StringBuilder expected = new StringBuilder();
        expected.append("{");
        expected.append("\"href\":\"/subscribers?_first=0&_max=10\",");
        expected.append("\"count\":3,");
        expected.append("\"results\":[");
        expected.append("{\"href\":\"/subscribers/" + s0.getId() + "\"}");
        expected.append("{\"href\":\"/subscribers/" + s1.getId() + "\"}");
        expected.append("{\"href\":\"/subscribers/" + s2.getId() + "\"}");
        expected.append("]");
        expected.append("}");

        assertEquals(expected.toString(), marshaller.marshal(resourceList, Subscriber.class));
    }
}
