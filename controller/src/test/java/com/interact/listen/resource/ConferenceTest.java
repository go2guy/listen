package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
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

    @After
    public void tearDown()
    {
        conference = null;
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        conference.setNumber(number);

        assertEquals(number, conference.getNumber());
    }

    @Test
    public void test_setAdminPin_withValidAdminPin_setsAdminPin()
    {
        final String adminPin = String.valueOf(System.currentTimeMillis());
        conference.setAdminPin(adminPin);

        assertEquals(adminPin, conference.getAdminPin());
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
        conference.validate();
        
        assertFalse(conference.hasErrors());
    }

    @Test
    public void test_validate_nullIsStarted_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setIsStarted(null);
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_nullAdminPin_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setAdminPin(null);
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_blankAdminPin_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setAdminPin("");
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_whitespaceAdminPin_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setAdminPin(" ");
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_nullNumber_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setNumber(null);
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_blankNumber_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setNumber("");
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsHasErrors()
    {
        conference = getPopulatedConference();
        conference.setNumber(" ");
        conference.validate();
        
        assertTrue(conference.hasErrors());
    }

    private Conference getPopulatedConference()
    {
        Conference c = new Conference();
        c.setAdminPin(String.valueOf(System.currentTimeMillis()));
        c.setId(System.currentTimeMillis());
        c.setIsStarted(Boolean.TRUE);
        c.setNumber(String.valueOf(System.currentTimeMillis()));
        c.setVersion(1);
        return c;
    }
}
