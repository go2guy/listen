package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void test_validate_validProperties_returnsNoErrors()
    {
        participant = getPopulatedParticipant();

        assertTrue(participant.validate());
        assertFalse(participant.hasErrors());
    }

    @Test
    public void test_validate_nullAudioResource_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setAudioResource(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullConference_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setConference(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsAdmin_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdmin(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsHolding_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsHolding(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsMuted(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullIsAdminMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdminMuted(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_isAdminAndIsAdminMuted_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setIsAdmin(Boolean.TRUE);
        participant.setIsAdminMuted(Boolean.TRUE);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_blankNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber("");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setNumber(" ");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_nullSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(null);

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_blankSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID("");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
    }

    @Test
    public void test_validate_whitespaceSessionID_returnsHasErrors()
    {
        participant = getPopulatedParticipant();
        participant.setSessionID(" ");

        assertFalse(participant.validate());
        assertTrue(participant.hasErrors());
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
