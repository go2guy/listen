package com.interact.listen.xml;

import static org.junit.Assert.assertEquals;

import com.interact.listen.resource.ResourceStub;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.xml.converter.Iso8601DateConverter;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.Test;

public class MarshallerTest
{
    private Marshaller marshaller;

    @Before
    public void setUp()
    {
        marshaller = new Marshaller();
    }

    @Test
    public void test_marshalShallow_withResourceStub_returnsShallowXml()
    {
        Long id = System.currentTimeMillis();
        ResourceStub resource = new ResourceStub();
        resource.setId(id);

        final String expected = "<resourceStub href=\"/resourceStub/" + id + "\"/>";
        assertEquals(expected, marshaller.marshalOpeningResourceTag(resource, true));
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
