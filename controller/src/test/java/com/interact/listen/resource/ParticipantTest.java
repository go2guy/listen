package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ParticipantTest
{
    private Participant participant;

    @Before
    public void setUp()
    {
        participant = new Participant();
    }

    @After
    public void tearDown()
    {
        participant = null;
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        participant.setId(id);

        assertEquals(id, participant.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        participant.setVersion(version);

        assertEquals(version, participant.getVersion());
    }

/*
    @Test
    public void test_loadFromXml_withoutConferenceAndLoadIdFalse_populatesParticipantWithoutConferenceAndId()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();
        final Date dateCreated = new Date();
        final Boolean isNew = Boolean.TRUE;

        final StringBuilder xml = new StringBuilder();
        xml.append("<participant>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");

        String formattedDate = new SimpleDateFormat(Participant.DATE_CREATED_FORMAT).format(dateCreated);
        xml.append("<dateCreated>").append(formattedDate).append("</dateCreated>");

        xml.append("<isNew>").append(isNew).append("</isNew>");
        xml.append("</participant>");

        participant.loadFromXml(xml.toString(), false);
        assertNull(participant.getConference());
        assertNull(participant.getId());
        assertEquals(fileLocation, participant.getFileLocation());
        assertEquals(dateCreated, participant.getDateCreated());
        assertEquals(isNew, participant.getIsNew());
    }

    @Test
    public void test_loadFromXml_withoutConferenceAndLoadIdTrue_populatesParticipantWithoutConference()
    {
        final Long id = System.currentTimeMillis();
        final String fileLocation = "/foo/bar/baz" + System.currentTimeMillis();
        final Date dateCreated = new Date();
        final Boolean isNew = Boolean.TRUE;

        final StringBuilder xml = new StringBuilder();
        xml.append("<participant>");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");

        String formattedDate = new SimpleDateFormat(Participant.DATE_CREATED_FORMAT).format(dateCreated);
        xml.append("<dateCreated>").append(formattedDate).append("</dateCreated>");

        xml.append("<isNew>").append(isNew).append("</isNew>");
        xml.append("</participant>");

        participant.loadFromXml(xml.toString(), true);
        assertNull(participant.getConference());
        assertEquals(id, participant.getId());
        assertEquals(fileLocation, participant.getFileLocation());
    }

*/
}
