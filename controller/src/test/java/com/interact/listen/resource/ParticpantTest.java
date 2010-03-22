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

    @Test
    public void test_toXml_withDeepTrueAndPropertiesSet_returnsXml()
    {
        // add a Conference to reference
        Conference conference = new Conference();
        conference.setId(System.currentTimeMillis());

        final long id = System.currentTimeMillis();
        final String number = "4024768786";
        final String fileLocation = "file:///some/file/path/audio.wav" + System.currentTimeMillis();
        final String sessionId = "2.0.CCXML_i0_d0.192.168.1.201.000CF1724D952385188032320126886075422"
        final Boolean yepitstrue = Boolean.TRUE;
        participant.setId(id);
        participant.setConference(conference);
        participant.setAudioResource(fileLocation);
        participant.setSessionID(sessionId);
        participant.setAdmin(yepitstrue);
        participant.setHolding(yepitstrue);
        participant.setMuted(yepitstrue);
        participant.setNumber(number);

        final StringBuilder expected = new StringBuilder();
        expected.append("<participant href=\"/participants/").append(id).append("\">");
        expected.append("<id>").append(id).append("</id>");
        expected.append("<conference href=\"/conferences/").append(conference.getId()).append("\"/>");
        expected.append("<number>").append(number).append("</number>");
        expected.append("<muted>").append(yesitstrue).append("</muted>");
        expected.append("<holding>").append(yesitstrue).append("</holding>");
        expected.append("<admin>").append(yesitstrue).append("</admin>");
        expected.append("<audioResource>").append(fileLocation).append("</audioResource>");
            xml.append("<sessionID>").append(sessionId).append("</sessionID>");
        expected.append("</participant>");

        assertEquals(expected.toString(), participant.toXml(true));
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
