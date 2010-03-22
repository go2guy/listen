package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        voicemail.setId(id);

        assertEquals(id, voicemail.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        voicemail.setVersion(version);

        assertEquals(version, voicemail.getVersion());
    }

    @Test
    public void test_setDateCreated_withValidDate_setsDateCreated()
    {
        final Date dateCreated = new Date();
        voicemail.setDateCreated(dateCreated);

        assertEquals(dateCreated, voicemail.getDateCreated());
    }

    @Test
    public void test_setIsNew_withValidBoolean_setsIsNew()
    {
        final Boolean isNew = Boolean.TRUE;
        voicemail.setIsNew(isNew);

        assertEquals(isNew, voicemail.getIsNew());
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
        final Date dateCreated = new Date();
        final Boolean isNew = Boolean.TRUE;
        voicemail.setId(id);
        voicemail.setSubscriber(subscriber);
        voicemail.setFileLocation(fileLocation);
        voicemail.setDateCreated(dateCreated);
        voicemail.setIsNew(isNew);

        final StringBuilder expected = new StringBuilder();
        expected.append("<voicemail href=\"/voicemails/").append(id).append("\">");
        expected.append("<id>").append(id).append("</id>");
        expected.append("<subscriber href=\"/subscribers/").append(subscriber.getId()).append("\"/>");
        expected.append("<fileLocation>").append(fileLocation).append("</fileLocation>");

        String formattedDate = new SimpleDateFormat(Voicemail.DATE_CREATED_FORMAT).format(dateCreated);
        expected.append("<dateCreated>").append(formattedDate).append("</dateCreated>");

        expected.append("<isNew>").append(isNew).append("</isNew>");
        expected.append("</voicemail>");

        assertEquals(expected.toString(), voicemail.toXml(true));
    }

    @Test
    public void test_loadFromXml_withoutSubscriberAndLoadIdFalse_populatesVoicemailWithoutSubscriberAndId()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();
        final Date dateCreated = new Date();
        final Boolean isNew = Boolean.TRUE;

        final StringBuilder xml = new StringBuilder();
        xml.append("<voicemail>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");

        String formattedDate = new SimpleDateFormat(Voicemail.DATE_CREATED_FORMAT).format(dateCreated);
        xml.append("<dateCreated>").append(formattedDate).append("</dateCreated>");

        xml.append("<isNew>").append(isNew).append("</isNew>");
        xml.append("</voicemail>");

        voicemail.loadFromXml(xml.toString(), false);
        assertNull(voicemail.getSubscriber());
        assertNull(voicemail.getId());
        assertEquals(fileLocation, voicemail.getFileLocation());
        assertEquals(dateCreated, voicemail.getDateCreated());
        assertEquals(isNew, voicemail.getIsNew());
    }

    @Test
    public void test_loadFromXml_withoutSubscriberAndLoadIdTrue_populatesVoicemailWithoutSubscriber()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();
        final Date dateCreated = new Date();
        final Boolean isNew = Boolean.TRUE;

        final StringBuilder xml = new StringBuilder();
        xml.append("<voicemail>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");

        String formattedDate = new SimpleDateFormat(Voicemail.DATE_CREATED_FORMAT).format(dateCreated);
        xml.append("<dateCreated>").append(formattedDate).append("</dateCreated>");

        xml.append("<isNew>").append(isNew).append("</isNew>");
        xml.append("</voicemail>");

        voicemail.loadFromXml(xml.toString(), true);
        assertNull(voicemail.getSubscriber());
        assertEquals(id, voicemail.getId());
        assertEquals(fileLocation, voicemail.getFileLocation());
    }
}
