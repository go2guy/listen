package com.interact.listen.xml;

import static org.junit.Assert.assertEquals;

import com.interact.listen.resource.ResourceStub;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import org.junit.Test;

public class MarshallerTest
{
    @Test
    public void test_marshalShallow_withResourceStub_returnsShallowXml()
    {
        Long id = System.currentTimeMillis();
        ResourceStub resource = new ResourceStub();
        resource.setId(id);

        final String expected = "<resourceStub href=\"/resourceStub/" + id + "\"/>";
        assertEquals(expected, Marshaller.marshalOpeningResourceTag(resource, true));
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

        StringBuilder expected = new StringBuilder();
        expected.append("<voicemail href=\"/voicemail/").append(voicemail.getId()).append("\">");
        expected.append("<dateCreated>").append(voicemail.getDateCreated()).append("</dateCreated>");
        expected.append("<fileLocation>").append(voicemail.getFileLocation()).append("</fileLocation>");
        expected.append("<id>").append(voicemail.getId()).append("</id>");
        expected.append("<isNew>").append(voicemail.getIsNew()).append("</isNew>");
        expected.append("<subscriber href=\"/subscriber/").append(subscriber.getId()).append("\"/>");
        expected.append("<version/>");
        expected.append("</voicemail>");

        assertEquals(expected.toString(), Marshaller.marshal(voicemail));
    }
}
