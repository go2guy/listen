package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class TimeRestrictionTest
{
    private TimeRestriction timeRestriction;

    @Before
    public void setUp()
    {
        timeRestriction = new TimeRestriction();
    }
    
    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        timeRestriction.setId(id);
        assertEquals(id, timeRestriction.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        timeRestriction.setVersion(version);
        assertEquals(version, timeRestriction.getVersion());
    }
    
    @Test
    public void test_setStartEntry_withValidStartEntry_setsStartEntry()
    {
        final String startEntry = "startEntry";
        timeRestriction.setStartEntry(startEntry);
        assertEquals(startEntry, timeRestriction.getStartEntry());
    }
    
    @Test
    public void test_setEndEntry_withValidEndEntry_setsEndEntry()
    {
        final String endEntry = "endEntry";
        timeRestriction.setEndEntry(endEntry);
        assertEquals(endEntry, timeRestriction.getEndEntry());
    }
    
    @Test
    public void test_setStartTime_withValidStartTime_setsStartTime()
    {
        final LocalTime startTime = new LocalTime(System.currentTimeMillis());
        timeRestriction.setStartTime(startTime);
        assertEquals(startTime, timeRestriction.getStartTime());
    }
    
    @Test
    public void test_setEndTime_withValidEndTime_setsEndTime()
    {
        final LocalTime endTime = new LocalTime(System.currentTimeMillis());
        timeRestriction.setEndTime(endTime);
        assertEquals(endTime, timeRestriction.getEndTime());
    }

    @Test
    public void test_setSubscriber_withValidSubscriber_setsSubscriber()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        timeRestriction.setSubscriber(subscriber);
        assertEquals(subscriber, timeRestriction.getSubscriber());
    }
    
    @Test
    public void test_validate_returnsTrueAndHasNoErrors()
    {
        timeRestriction = getPopulatedTimeRestriction();
        assertTrue(timeRestriction.validate());
        assertFalse(timeRestriction.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        timeRestriction = getPopulatedTimeRestriction();
        TimeRestriction copy = timeRestriction.copy(false);

        assertNull(copy.getId());
        assertEquals((Integer)0, copy.getVersion());
        assertEquals(timeRestriction.getStartEntry(), copy.getStartEntry());
        assertEquals(timeRestriction.getEndEntry(), copy.getEndEntry());
        assertEquals(timeRestriction.getStartTime(), copy.getStartTime());
        assertEquals(timeRestriction.getEndTime(), copy.getEndTime());
        assertEquals(timeRestriction.getSubscriber(), copy.getSubscriber());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        timeRestriction = getPopulatedTimeRestriction();
        TimeRestriction copy = timeRestriction.copy(true);

        assertEquals(timeRestriction.getId(), copy.getId());
        assertEquals(timeRestriction.getVersion(), copy.getVersion());
        assertEquals(timeRestriction.getStartEntry(), copy.getStartEntry());
        assertEquals(timeRestriction.getEndEntry(), copy.getEndEntry());
        assertEquals(timeRestriction.getStartTime(), copy.getStartTime());
        assertEquals(timeRestriction.getEndTime(), copy.getEndTime());
        assertEquals(timeRestriction.getSubscriber(), copy.getSubscriber());
    }
    
    private TimeRestriction getPopulatedTimeRestriction()
    {
        TimeRestriction tr = new TimeRestriction();
        tr.setStartEntry(String.valueOf(System.currentTimeMillis()));
        tr.setEndEntry(String.valueOf(System.currentTimeMillis()));
        tr.setStartTime(new LocalTime(System.currentTimeMillis()));
        tr.setEndTime(new LocalTime(System.currentTimeMillis()));
        tr.setId(System.currentTimeMillis());
        tr.setSubscriber(new Subscriber());
        tr.setVersion(10);
        return tr;
    }
}
