package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VoicemailTest
{
    private Voicemail voicemail;

    @Before
    public void setUp()
    {
        voicemail = new Voicemail();
    }

    @After
    public void tearDown()
    {
        voicemail = null;
    }

    @Test
    public void test_toXml_withDeepFalseAndIdSet_returnsXml()
    {
        final long id = System.currentTimeMillis();
        voicemail.setId(id);

        final String expected = "<voicemail href=\"/voicemails/" + id + "\"/>";
        assertEquals(expected, voicemail.toXml(false));
    }

    @Test
    public void test_toXml_withDeepTrueAndPropertiesSet_returnsXml()
    {
        // add a subscriber to reference
        Subscriber subscriber = new Subscriber();
        subscriber.setId(System.currentTimeMillis());

        final long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz/" + System.currentTimeMillis();
        voicemail.setId(id);
        voicemail.setSubscriber(subscriber);
        voicemail.setFileLocation(fileLocation);

        final StringBuilder expected = new StringBuilder();
        expected.append("<voicemail href=\"/voicemails/").append(id).append("\">");
        expected.append("<id>").append(id).append("</id>");
        expected.append("<subscriber href=\"/subscribers/").append(subscriber.getId()).append("\"/>");
        expected.append("<fileLocation>").append(fileLocation).append("</fileLocation>");
        expected.append("</voicemail>");

        assertEquals(expected.toString(), voicemail.toXml(true));
    }

    @Test
    public void test_loadFromXml_withoutSubscriberAndLoadIdFalse_populatesVoicemailWithoutSubscriberAndId()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();

        final StringBuilder xml = new StringBuilder();
        xml.append("<voicemail>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");
        xml.append("</voicemail>");

        voicemail.loadFromXml(xml.toString(), false);
        assertNull(voicemail.getSubscriber());
        assertNull(voicemail.getId());
        assertEquals(fileLocation, voicemail.getFileLocation());
    }

    @Test
    public void test_loadFromXml_withoutSubscriberAndLoadIdTrue_populatesVoicemailWithoutSubscriber()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();

        final StringBuilder xml = new StringBuilder();
        xml.append("<voicemail>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");
        xml.append("</voicemail>");

        voicemail.loadFromXml(xml.toString(), true);
        assertNull(voicemail.getSubscriber());
        assertEquals(id, voicemail.getId());
        assertEquals(fileLocation, voicemail.getFileLocation());
    }
}
