package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ConferenceTest
{
    private Conference conference;

    @Before
    public void setUp()
    {
        conference = new Conference();
    }

    @Test
    public void test_setIsStarted_withValidIsStarted_setsIsStarted()
    {
        final Boolean isStarted = true;
        conference.setIsStarted(isStarted);

        assertEquals(isStarted, conference.getIsStarted());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        conference.setId(id);

        assertEquals(id, conference.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        conference.setVersion(version);

        assertEquals(version, conference.getVersion());
    }

    @Test
    public void test_validate_validParameters_returnsNoErrors()
    {
        conference = getPopulatedConference();

        assertTrue(conference.validate());
        assertFalse(conference.hasErrors());
    }

    @Test
    public void test_validate_nullIsStarted_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setIsStarted(null);

        assertFalse(conference.validate());
        assertTrue(conference.hasErrors());
    }

    private Conference getPopulatedConference()
    {
        Conference c = new Conference();
        c.setId(System.currentTimeMillis());
        c.setIsStarted(Boolean.TRUE);
        c.setVersion(1);
        return c;
    }
}
