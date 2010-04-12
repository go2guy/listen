package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class VoicemailTest
{
    private Voicemail voicemail;

    @Before
    public void setUp()
    {
        voicemail = new Voicemail();
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        voicemail.setId(id);

        assertEquals(id, voicemail.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        voicemail.setVersion(version);

        assertEquals(version, voicemail.getVersion());
    }

    @Test
    public void test_setDateCreated_withValidDate_setsDateCreated()
    {
        final Date dateCreated = new Date();
        voicemail.setDateCreated(dateCreated);

        assertEquals(dateCreated, voicemail.getDateCreated());
    }

    @Test
    public void test_setIsNew_withValidBoolean_setsIsNew()
    {
        final Boolean isNew = Boolean.TRUE;
        voicemail.setIsNew(isNew);

        assertEquals(isNew, voicemail.getIsNew());
    }

    @Test
    public void test_validate_validProperties_returnsNoErrors()
    {
        voicemail = getPopulatedVoicemail();

        assertTrue(voicemail.validate());
        assertFalse(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullSubscriber_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setSubscriber(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullDateCreated_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setDateCreated(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullFileLocation_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setFileLocation(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_blankFileLocation_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setFileLocation("");

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_whitespaceFileLocation_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setFileLocation("  ");

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    @Test
    public void test_validate_nullIsNew_returnsHasErrors()
    {
        voicemail = getPopulatedVoicemail();
        voicemail.setIsNew(null);

        assertFalse(voicemail.validate());
        assertTrue(voicemail.hasErrors());
    }

    private Voicemail getPopulatedVoicemail()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);
        s.setNumber(String.valueOf(System.currentTimeMillis()));

        Voicemail v = new Voicemail();
        v.setDateCreated(new Date());
        v.setFileLocation("/foo/bar/baz");
        v.setId(System.currentTimeMillis());
        v.setIsNew(Boolean.TRUE);
        v.setSubscriber(s);
        v.setVersion(1);

        return v;
    }
}
