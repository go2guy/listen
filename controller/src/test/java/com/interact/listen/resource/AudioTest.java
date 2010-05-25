package com.interact.listen.resource;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class AudioTest
{
    private Audio audio;

    @Before
    public void setUp()
    {
        audio = new Audio();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), audio.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        audio.setId(id);

        assertEquals(id, audio.getId());
    }

    @Test
    public void test_setUri_withValidUri_setsUri()
    {
        final String uri = "foo";
        audio.setUri(uri);

        assertEquals(uri, audio.getUri());
    }
    
    @Test
    public void test_setDescription_withValidDescription_setsDescription()
    {
        final String description = "foo";
        audio.setDescription(description);

        assertEquals(description, audio.getDescription());
    }

    @Test
    public void test_setFileSize_withValidFileSize_setsFileSize()
    {
        final String fileSize = "foo";
        audio.setFileSize(fileSize);

        assertEquals(fileSize, audio.getFileSize());
    }
    
    @Test
    public void test_setDateCreated_withValidDate_setsDateCreated()
    {
        final Date dateCreated = new Date();
        audio.setDateCreated(dateCreated);

        assertEquals(dateCreated, audio.getDateCreated());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        audio.setVersion(version);

        assertEquals(version, audio.getVersion());
    }

    @Test
    public void test_validate_validParameters_returnsNoErrors()
    {
        audio = getPopulatedAudio();

        assertTrue(audio.validate());
        assertFalse(audio.hasErrors());
    }
    
    @Test
    public void test_validate_nullUri_returnsTrueAndHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setUri(null);

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }

    @Test
    public void test_validate_blankUri_returnsTrueAndHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setUri(" ");

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }

    @Test
    public void test_validate_nullDescription_returnsTrueAndHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setDescription(null);

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }

    @Test
    public void test_validate_blankDescription_returnsTrueAndHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setDescription(" ");

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }
    
    @Test
    public void test_validate_nullFileSize_returnsHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setFileSize(null);

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }
    
    @Test
    public void test_validate_blankFileSize_returnsTrueAndHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setFileSize(" ");

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }
    
    @Test
    public void test_validate_nullDateCreated_returnsHasErrors()
    {
        audio = getPopulatedAudio();
        audio.setDateCreated(null);

        assertFalse(audio.validate());
        assertTrue(audio.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        Audio original = getPopulatedAudio();
        Audio copy = original.copy(false);

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
        Audio original = getPopulatedAudio();
        Audio copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(audio.equals(audio));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(audio.equals(null));
    }

    @Test
    public void test_equals_thatNotAudio_returnsFalse()
    {
        assertFalse(audio.equals(new String()));
    }
    
    @Test
    public void test_hashCode_returnsUniqueHashCodeForRelevantFields()
    {
        Audio obj = new Audio();

        // hashcode-relevant properties set to static values for predictability
        obj.setUri("Sorry Charlie");

        // set a property that has no effect on hashcode to something dynamic
        obj.setDateCreated(new Date());
        
        assertEquals(1624358030, obj.hashCode());
    }

    private Audio getPopulatedAudio()
    {
        Audio a = new Audio();
        a.setUri(String.valueOf(System.currentTimeMillis()));
        a.setId(System.currentTimeMillis());
        a.setDescription(String.valueOf(System.currentTimeMillis()));
        a.setFileSize(String.valueOf(System.currentTimeMillis()));
        a.setVersion(1);
        return a;
    }
}
