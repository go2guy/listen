package com.interact.listen.resource;

import static org.junit.Assert.*;

import java.util.Date;

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
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), conferenceHistory.getVersion());
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
    public void test_setSubscriber_withValidSubscriber_setsSubscriber()
    {
        final String subscriber = String.valueOf(System.currentTimeMillis());
        conferenceHistory.setSubscriber(subscriber);

        assertEquals(subscriber, conferenceHistory.getSubscriber());
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
    public void test_validate_withNullSubscriber_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setSubscriber(null);

        assertFalse(conferenceHistory.validate());
        assertTrue(conferenceHistory.hasErrors());
    }

    @Test
    public void test_validate_withBlankSubscriber_returnsFalseAndHasErrors()
    {
        conferenceHistory = getPopulatedConferenceHistory();
        conferenceHistory.setSubscriber(" ");

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

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithAllPropertiesExceptIdAndVersion()
    {
        ConferenceHistory original = getPopulatedConferenceHistory();
        ConferenceHistory copy = original.copy(false);

        assertTrue(original.getConference() == copy.getConference()); // same reference
        assertEquals(original.getDateCreated(), copy.getDateCreated());
        assertFalse(original.getDateCreated() == copy.getDateCreated()); // different reference
        assertEquals(original.getDescription(), copy.getDescription());
        assertTrue(original.getSubscriber() == copy.getSubscriber()); // same reference

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withNullDateCreated_setsNullDateCreated()
    {
        ConferenceHistory original = getPopulatedConferenceHistory();
        original.setDateCreated(null);
        ConferenceHistory copy = original.copy(false);

        assertNull(copy.getDateCreated());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        ConferenceHistory original = getPopulatedConferenceHistory();
        ConferenceHistory copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(conferenceHistory.equals(conferenceHistory));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(conferenceHistory.equals(null));
    }

    @Test
    public void test_equals_thatNotAConferenceHistory_returnsFalse()
    {
        assertFalse(conferenceHistory.equals(new String()));
    }

    @Test
    public void test_equals_conferenceNotEqual_returnsFalse()
    {
        conferenceHistory.setConference(new Conference());

        ConferenceHistory that = new ConferenceHistory();
        that.setConference(null);

        assertFalse(conferenceHistory.equals(that));
    }

    @Test
    public void test_equals_dateCreatedNotEqual_returnsFalse()
    {
        conferenceHistory.setDateCreated(new Date());

        ConferenceHistory that = new ConferenceHistory();
        that.setDateCreated(null);

        assertFalse(conferenceHistory.equals(that));
    }

    @Test
    public void test_equals_descriptionNotEqual_returnsFalse()
    {
        conferenceHistory.setDescription(String.valueOf(System.currentTimeMillis()));

        ConferenceHistory that = new ConferenceHistory();
        that.setDescription(null);

        assertFalse(conferenceHistory.equals(that));
    }

    @Test
    public void test_equals_subscriberNotEqual_returnsFalse()
    {
        conferenceHistory.setSubscriber(String.valueOf(System.currentTimeMillis()));

        ConferenceHistory that = new ConferenceHistory();
        that.setSubscriber(null);

        assertFalse(conferenceHistory.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        Conference conference = new Conference();
        Date dateCreated = new Date();
        String description = String.valueOf(System.currentTimeMillis());
        String subscriber = String.valueOf(System.currentTimeMillis());

        conferenceHistory.setConference(conference);
        conferenceHistory.setDateCreated(dateCreated);
        conferenceHistory.setDescription(description);
        conferenceHistory.setSubscriber(subscriber);

        ConferenceHistory that = new ConferenceHistory();
        that.setConference(conference);
        that.setDateCreated(dateCreated);
        that.setDescription(description);
        that.setSubscriber(subscriber);

        assertTrue(conferenceHistory.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashcodeForRelevantFields()
    {
        ConferenceHistory obj = new ConferenceHistory();

        // hashcode-relevant properties set to static values for predictability
        obj.setConference(new Conference());
        obj.setDateCreated(new Date(Long.valueOf("1273861270512")));
        obj.setDescription("Pepe");
        obj.setSubscriber("Big John");

        // set a property that has no effect on hashcode to something dynamic
        obj.setId(System.currentTimeMillis());

        assertEquals(-1955306176, obj.hashCode());
    }

    private ConferenceHistory getPopulatedConferenceHistory()
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setId(System.currentTimeMillis());
        history.setVersion(1);
        history.setDescription("All Cows Eat Grass");
        history.setSubscriber("Good Boys Do Fine Always");
        history.setConference(new Conference());
        return history;
    }
}
