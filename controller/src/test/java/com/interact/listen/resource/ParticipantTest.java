package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void test_validate_validProperties_returnsTrue()
    {
        participant = getPopulatedParticipant();
        assertTrue(participant.validate());
    }

    @Test
    public void test_validate_nullAudioResource_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setAudioResource(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullConference_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setConference(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullIsAdmin_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdmin(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullIsHolding_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setIsHolding(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullIsMuted_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setIsMuted(null);
        assertFalse(participant.validate());
    }
    
    @Test
    public void test_validate_nullIsAdminMuted_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdminMuted(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullNumber_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_blankNumber_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setNumber("");
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(" ");
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_nullSessionID_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(null);
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_blankSessionID_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID("");
        assertFalse(participant.validate());
    }

    @Test
    public void test_validate_whitespaceSessionID_returnsFalse()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(" ");
        assertFalse(participant.validate());
    }

    private Participant getPopulatedParticipant()
    {
        Conference c = new Conference();
        c.setAdminPin(String.valueOf(System.currentTimeMillis()));
        c.setId(System.currentTimeMillis());
        c.setIsStarted(Boolean.TRUE);
        c.setNumber(String.valueOf(System.currentTimeMillis()));
        c.setVersion(1);

        Participant p = new Participant();
        p.setAudioResource("/audio/resource");
        p.setConference(c);
        p.setId(System.currentTimeMillis());
        p.setIsAdmin(Boolean.TRUE);
        p.setIsHolding(Boolean.TRUE);
        p.setIsMuted(Boolean.TRUE);
        p.setIsAdminMuted(Boolean.FALSE);
        p.setNumber(String.valueOf(System.currentTimeMillis()));
        p.setSessionID(String.valueOf(System.currentTimeMillis()));
        p.setVersion(1);

        return p;
    }
}
