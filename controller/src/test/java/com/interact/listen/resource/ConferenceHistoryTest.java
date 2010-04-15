package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ConferenceHistoryTest
{
    private ConferenceHistory conferenceHistory;

    @Before
    public void setUp()
    {
        conferenceHistory = new ConferenceHistory();
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        conferenceHistory.setId(id);

        assertEquals(id, conferenceHistory.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        conferenceHistory.setVersion(version);

        assertEquals(version, conferenceHistory.getVersion());
    }

    @Test
    public void test_setDescription_withValidDescription_setsDescription()
    {
        final String description = String.valueOf(System.currentTimeMillis());
        conferenceHistory.setDescription(description);

        assertEquals(description, conferenceHistory.getDescription());
    }

    @Test
    public void test_setConference_withValidConference_setsConference()
    {
        final Conference conference = new Conference();
        conferenceHistory.setConference(conference);

        assertTrue(conference == conferenceHistory.getConference()); // object comparison
    }

    @Test
    public void test_setUser_withValidUser_setsUser()
    {
        final String user = String.valueOf(System.currentTimeMillis());
        conferenceHistory.setUser(user);

        assertEquals(user, conferenceHistory.getUser());
    }

    @Test
    public void test_validate_withValidConferenceHistory_returnsTrueAndHasNoErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();

        assertTrue(conferenceHistory.validate());
        assertFalse(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withNullConference_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setConference(null);

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withNullUser_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setUser(null);

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withBlankUser_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setUser(" ");

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withNullDescription_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setDescription(null);

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withBlankDescription_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setDescription(" ");

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    private ConferenceHistory getPopulatedConferenceHistory()
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setId(System.currentTimeMillis());
        history.setVersion(1);
        history.setDescription("All Cows Eat Grass");
        history.setUser("Good Boys Do Fine Always");
        history.setConference(new Conference());
        return history;
    }
}
