package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.After;
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

    @After
    public void tearDown()
    {
        voicemail = null;
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
}
