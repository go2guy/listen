package com.interact.listen.resource;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ConferenceRecordingTest
{
    private ConferenceRecording conferenceRecording;

    @Before
    public void setUp()
    {
        conferenceRecording = new ConferenceRecording();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), conferenceRecording.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        conferenceRecording.setId(id);

        assertEquals(id, conferenceRecording.getId());
    }

    @Test
    public void test_setUri_withValidUri_setsUri()
    {
        final String uri = "foo";
        conferenceRecording.setUri(uri);

        assertEquals(uri, conferenceRecording.getUri());
    }

    @Test
    public void test_setDescription_withValidDescription_setsDescription()
    {
        final String description = "foo";
        conferenceRecording.setDescription(description);

        assertEquals(description, conferenceRecording.getDescription());
    }

    @Test
    public void test_setFileSize_withValidFileSize_setsFileSize()
    {
        final String fileSize = "foo";
        conferenceRecording.setFileSize(fileSize);

        assertEquals(fileSize, conferenceRecording.getFileSize());
    }

    @Test
    public void test_setDateCreated_withValidDate_setsDateCreated()
    {
        final Date dateCreated = new Date();
        conferenceRecording.setDateCreated(dateCreated);

        assertEquals(dateCreated, conferenceRecording.getDateCreated());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        conferenceRecording.setVersion(version);

        assertEquals(version, conferenceRecording.getVersion());
    }

    @Test
    public void test_validate_validParameters_returnsNoErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();

        assertTrue(conferenceRecording.validate());
        assertFalse(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_nullUri_returnsTrueAndHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setUri(null);

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_blankUri_returnsTrueAndHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setUri(" ");

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_nullDescription_returnsTrueAndHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setDescription(null);

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_blankDescription_returnsTrueAndHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setDescription(" ");

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_nullFileSize_returnsHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setFileSize(null);

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_blankFileSize_returnsTrueAndHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setFileSize(" ");

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_validate_nullDateCreated_returnsHasErrors()
    {
        conferenceRecording = getPopulatedConferenceRecording();
        conferenceRecording.setDateCreated(null);

        assertFalse(conferenceRecording.validate());
        assertTrue(conferenceRecording.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        ConferenceRecording original = getPopulatedConferenceRecording();
        ConferenceRecording copy = original.copy(false);

        assertEquals(original.getUri(), copy.getUri());
        assertEquals(original.getDescription(), copy.getDescription());
        assertEquals(original.getFileSize(), copy.getFileSize());
        assertEquals(original.getDateCreated(), copy.getDateCreated());
        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        ConferenceRecording original = getPopulatedConferenceRecording();
        ConferenceRecording copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(conferenceRecording.equals(conferenceRecording));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(conferenceRecording.equals(null));
    }

    @Test
    public void test_equals_thatNotAudio_returnsFalse()
    {
        assertFalse(conferenceRecording.equals(new String()));
    }

    @Test
    public void test_hashCode_returnsUniqueHashCodeForRelevantFields()
    {
        ConferenceRecording obj = new ConferenceRecording();

        // hashcode-relevant properties set to static values for predictability
        obj.setUri("Sorry Charlie");

        // set a property that has no effect on hashcode to something dynamic
        obj.setDateCreated(new Date());

        assertEquals(1624358030, obj.hashCode());
    }

    private ConferenceRecording getPopulatedConferenceRecording()
    {
        ConferenceRecording a = new ConferenceRecording();
        a.setConference(new Conference());
        a.setId(System.currentTimeMillis());
        a.setDescription(String.valueOf(System.currentTimeMillis()));
        a.setFileSize(String.valueOf(System.currentTimeMillis()));
        a.setUri(String.valueOf(System.currentTimeMillis()));
        a.setVersion(1);
        return a;
    }
}
