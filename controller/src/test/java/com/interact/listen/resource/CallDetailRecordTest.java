package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class CallDetailRecordTest
{
    private CallDetailRecord cdr;

    @Before
    public void setUp()
    {
        cdr = new CallDetailRecord();
    }

    @Test
    public void test_dateStarted_defaultsToNewDate()
    {
        assertNotNull(cdr.getDateStarted());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        cdr.setId(id);
        assertEquals(id, cdr.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        cdr.setVersion(version);
        assertEquals(version, cdr.getVersion());
    }

    @Test
    public void test_setSubscriber_withValidSubscriber_setsSubscriber()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        cdr.setSubscriber(subscriber);
        assertEquals(subscriber, cdr.getSubscriber());
    }

    @Test
    public void test_setDuration_withValidDuration_setsDuration()
    {
        final Long duration = System.currentTimeMillis();
        cdr.setDuration(duration);
        assertEquals(duration, cdr.getDuration());
    }

    @Test
    public void test_setService_withValidService_setsService()
    {
        final String service = String.valueOf(System.currentTimeMillis());
        cdr.setService(service);
        assertEquals(service, cdr.getService());
    }

    @Test
    public void test_setAni_withValidAni_setsAni()
    {
        final String ani = String.valueOf(System.currentTimeMillis());
        cdr.setAni(ani);
        assertEquals(ani, cdr.getAni());
    }

    @Test
    public void test_setDnis_withValidDnis_setsDnis()
    {
        final String dnis = String.valueOf(System.currentTimeMillis());
        cdr.setDnis(dnis);
        assertEquals(dnis, cdr.getDnis());
    }

    @Test
    public void test_validate_withValidProperties_returnsTrueAndHasNoErrors()
    {
        cdr = getPopulatedCdr();
        assertTrue(cdr.validate());
        assertFalse(cdr.hasErrors());
    }

    @Test
    public void test_validate_returnsTrueAndHasNoErrors()
    {
        cdr = getPopulatedCdr();
        assertTrue(cdr.validate());
        assertFalse(cdr.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        cdr = getPopulatedCdr();
        CallDetailRecord copy = cdr.copy(false);

        assertNull(copy.getId());
        assertEquals((Integer)0, copy.getVersion());
        assertEquals(cdr.getAni(), copy.getAni());
        assertEquals(cdr.getDateStarted(), copy.getDateStarted());
        assertEquals(cdr.getDnis(), copy.getDnis());
        assertEquals(cdr.getService(), copy.getService());
        assertEquals(cdr.getSubscriber(), copy.getSubscriber());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        cdr = getPopulatedCdr();
        CallDetailRecord copy = cdr.copy(true);

        assertEquals(cdr.getId(), copy.getId());
        assertEquals(cdr.getVersion(), copy.getVersion());
        assertEquals(cdr.getAni(), copy.getAni());
        assertEquals(cdr.getDateStarted(), copy.getDateStarted());
        assertEquals(cdr.getDnis(), copy.getDnis());
        assertEquals(cdr.getService(), copy.getService());
        assertEquals(cdr.getSubscriber(), copy.getSubscriber());
    }

    @Test
    public void test_equals_withRelevantPropertiesEqual_returnsTrue()
    {
        cdr.setAni(String.valueOf(System.currentTimeMillis()));
        cdr.setDnis(String.valueOf(System.currentTimeMillis()));
        cdr.setDateStarted(new Date());

        // set relevant properties equal
        CallDetailRecord that = new CallDetailRecord();
        that.setAni(cdr.getAni());
        that.setDnis(cdr.getDnis());
        that.setDateStarted(cdr.getDateStarted());

        // set irrelevant property to something different
        cdr.setDuration(System.currentTimeMillis() - 1000);
        that.setDuration(System.currentTimeMillis());

        assertTrue(cdr.equals(that));
    }

    @Test
    public void test_equals_differentAni_returnsFalse()
    {
        cdr.setAni(String.valueOf(System.currentTimeMillis()));
        cdr.setDnis(String.valueOf(System.currentTimeMillis()));
        cdr.setDateStarted(new Date());

        // set relevant properties equal
        CallDetailRecord that = new CallDetailRecord();
        that.setAni("");
        that.setDnis(cdr.getDnis());
        that.setDateStarted(cdr.getDateStarted());

        // set irrelevant property to something different
        cdr.setDuration(System.currentTimeMillis() - 1000);
        that.setDuration(System.currentTimeMillis());

        assertFalse(cdr.equals(that));
    }

    @Test
    public void test_equals_differentDnis_returnsFalse()
    {
        cdr.setAni(String.valueOf(System.currentTimeMillis()));
        cdr.setDnis(String.valueOf(System.currentTimeMillis()));
        cdr.setDateStarted(new Date());

        // set relevant properties equal
        CallDetailRecord that = new CallDetailRecord();
        that.setAni(cdr.getAni());
        that.setDnis("");
        that.setDateStarted(cdr.getDateStarted());

        // set irrelevant property to something different
        cdr.setDuration(System.currentTimeMillis() - 1000);
        that.setDuration(System.currentTimeMillis());

        assertFalse(cdr.equals(that));
    }

    @Test
    public void test_equals_differentDateStarted_returnsFalse()
    {
        cdr.setAni(String.valueOf(System.currentTimeMillis()));
        cdr.setDnis(String.valueOf(System.currentTimeMillis()));
        cdr.setDateStarted(new Date());

        // set relevant properties equal
        CallDetailRecord that = new CallDetailRecord();
        that.setAni(cdr.getAni());
        that.setDnis(cdr.getDnis());
        that.setDateStarted(null);

        // set irrelevant property to something different
        cdr.setDuration(System.currentTimeMillis() - 1000);
        that.setDuration(System.currentTimeMillis());

        assertFalse(cdr.equals(that));
    }

    @Test
    public void test_hashCode()
    {
        cdr.setAni("4321");
        cdr.setDnis("1234");
        cdr.setDateStarted(new Date(12341234));
        assertEquals(1491766221, cdr.hashCode());
    }

    private CallDetailRecord getPopulatedCdr()
    {
        CallDetailRecord c = new CallDetailRecord();
        c.setAni(String.valueOf(System.currentTimeMillis()));
        c.setDateStarted(new Date());
        c.setDnis(String.valueOf(System.currentTimeMillis()));
        c.setDuration(System.currentTimeMillis());
        c.setId(System.currentTimeMillis());
        c.setService(String.valueOf(System.currentTimeMillis()));
        c.setSubscriber(new Subscriber());
        c.setVersion(10);
        return c;
    }
}
