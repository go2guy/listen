package com.interact.listen.marshal.xml;

import static org.junit.Assert.assertEquals;

import com.interact.listen.marshal.converter.Iso8601DateConverter;
import com.interact.listen.resource.ResourceStub;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

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
        expected.append("<voicemail href=\"/voicemail/").append(voicemail.getId()).append("\">");
        expected.append("<dateCreated>").append(formattedDate).append("</dateCreated>");
        expected.append("<fileLocation>").append(voicemail.getFileLocation()).append("</fileLocation>");
        expected.append("<id>").append(voicemail.getId()).append("</id>");
        expected.append("<isNew>").append(voicemail.getIsNew()).append("</isNew>");
        expected.append("<subscriber href=\"/subscriber/").append(subscriber.getId()).append("\"/>");
        expected.append("<version>").append(voicemail.getVersion()).append("</version>");
        expected.append("</voicemail>");

        assertEquals(expected.toString(), marshaller.marshal(voicemail));
    }

    @Test
    public void test_unmarshal()
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());
        subscriber.setNumber("foo" + System.currentTimeMillis());

        String xml = marshaller.marshal(subscriber);
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());

        marshaller.unmarshal(stream);
    }
}
