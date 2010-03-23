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
}
