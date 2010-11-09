package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ScheduledConferenceTest extends ListenTest
{
    private ScheduledConference scheduledConference;
    
    @Before
    public void setUp()
    {
        scheduledConference = new ScheduledConference();
    }
    
    @Test
    public void test_setId_setsId()
    {
        Long id = TestUtil.randomNumeric(10);
        scheduledConference.setId(id);
        assertEquals(id, scheduledConference.getId());
    }
    
    @Test
    public void test_setVersion_setsVersion()
    {
        Integer version = 10;
        scheduledConference.setVersion(version);
        assertEquals(version, scheduledConference.getVersion());
    }
    
    @Test
    public void test_setStartDate_setsStartDate()
    {
        Date startDate = new Date();
        scheduledConference.setStartDate(startDate);
        assertEquals(startDate.getTime(), scheduledConference.getStartDate().getTime());
        assertNotSame(startDate, scheduledConference.getStartDate());
    }
    
    @Test
    public void test_setStartDate_withNull_setsNullWithoutException()
    {
        scheduledConference.setStartDate(null);
        assertNull(scheduledConference.getStartDate());
    }
    
    @Test
    public void test_setEndDate_setsEndDate()
    {
        Date endDate = new Date();
        scheduledConference.setEndDate(endDate);
        assertEquals(endDate.getTime(), scheduledConference.getEndDate().getTime());
        assertNotSame(endDate, scheduledConference.getEndDate());
    }
    
    @Test
    public void test_setEndDate_withNull_setsNullWithoutException()
    {
        scheduledConference.setEndDate(null);
        assertNull(scheduledConference.getEndDate());
    }
    
    @Test
    public void test_setTopic_setsTopic()
    {
        String topic = TestUtil.randomString();
        scheduledConference.setTopic(topic);
        assertEquals(topic, scheduledConference.getTopic());
    }
    
    @Test
    public void test_setNotes_setsNotes()
    {
        String notes = TestUtil.randomString();
        scheduledConference.setNotes(notes);
        assertEquals(notes, scheduledConference.getNotes());
    }
    
    @Test
    public void test_setConference_setsConference()
    {
        Conference conference = new Conference();
        scheduledConference.setConference(conference);
        assertEquals(conference, scheduledConference.getConference());
    }
    
    @Test
    public void test_setScheduledBy_setsScheduledBy()
    {
        Subscriber subscriber = new Subscriber();
        scheduledConference.setScheduledBy(subscriber);
        assertEquals(subscriber, scheduledConference.getScheduledBy());
    }
    
    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);
        ScheduledConference original = createScheduledConference(session, conference, subscriber);

        ScheduledConference copy = (ScheduledConference)original.copy(true);

        assertEquals(original.getConference(), copy.getConference());
        assertEquals(original.getEndDate(), copy.getEndDate());
        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getNotes(), copy.getNotes());
        assertEquals(original.getScheduledBy(), copy.getScheduledBy());
        assertEquals(original.getStartDate(), copy.getStartDate());
        assertEquals(original.getTopic(), copy.getTopic());
        assertEquals(original.getVersion(), copy.getVersion());
    }
    
    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        Subscriber subscriber = createSubscriber(session);
        Conference conference = createConference(session, subscriber);
        ScheduledConference original = createScheduledConference(session, conference, subscriber);

        ScheduledConference copy = (ScheduledConference)original.copy(false);

        assertEquals(original.getConference(), copy.getConference());
        assertEquals(original.getEndDate(), copy.getEndDate());
        assertEquals(original.getNotes(), copy.getNotes());
        assertEquals(original.getScheduledBy(), copy.getScheduledBy());
        assertEquals(original.getStartDate(), copy.getStartDate());
        assertEquals(original.getTopic(), copy.getTopic());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }
    
    @Test
    public void test_validate_withNullStartDate_returnsFalseAndHasErrors()
    {
        scheduledConference.setStartDate(null);
        scheduledConference.setEndDate(new Date());
        scheduledConference.setConference(new Conference());
        
        assertFalse(scheduledConference.validate());
        assertTrue(scheduledConference.errors().contains("Start Date cannot be null"));
    }
    
    @Test
    public void test_validate_withNullEndDate_returnsFalseAndHasErrors()
    {
        scheduledConference.setStartDate(new Date());
        scheduledConference.setEndDate(null);
        scheduledConference.setConference(new Conference());
        
        assertFalse(scheduledConference.validate());
        assertTrue(scheduledConference.errors().contains("End Date cannot be null"));
    }
    
    @Test
    public void test_validate_withNullConference_returnsFalseAndHasErrors()
    {
        scheduledConference.setStartDate(new Date());
        scheduledConference.setEndDate(new Date());
        scheduledConference.setConference(null);
        
        assertFalse(scheduledConference.validate());
        assertTrue(scheduledConference.errors().contains("Conference cannot be null"));
    }
}
