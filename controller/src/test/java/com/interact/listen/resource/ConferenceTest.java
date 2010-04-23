package com.interact.listen.resource;

import static org.junit.Assert.*;
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
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), conference.getVersion());
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

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        Conference original = getPopulatedConference();
        Conference copy = original.copy(false);

        assertTrue(original.getConferenceHistorys() == copy.getConferenceHistorys()); // same reference
        assertEquals(original.getIsStarted(), copy.getIsStarted());
        assertTrue(original.getParticipants() == copy.getParticipants()); // same reference
        
        // pins cannot be the same reference - in fact, i don't think any collection can
        // FIXME make all collections and references deep copies
        assertFalse(original.getPins() == copy.getPins()); // different reference
        // TODO containsAll check here

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        Conference original = getPopulatedConference();
        Conference copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
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
