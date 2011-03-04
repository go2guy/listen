package com.interact.listen.resource;

import static org.junit.Assert.*;

import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class CallRestrictionTest
{
    private CallRestriction callRestriction;

    @Before
    public void setUp()
    {
        callRestriction = new CallRestriction();
    }
    
    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        callRestriction.setId(id);
        assertEquals(id, callRestriction.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        callRestriction.setVersion(version);
        assertEquals(version, callRestriction.getVersion());
    }
    
    @Test
    public void test_setDestination_withValidDestination_setsDestination()
    {
        final String destination = "destination";
        callRestriction.setDestination(destination);
        assertEquals(destination, callRestriction.getDestination());
    }
    
    @Test
    public void test_setSubscriber_withValidSubscriber_setsSubscriber()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        callRestriction.setSubscriber(subscriber);
        assertEquals(subscriber, callRestriction.getSubscriber());
    }
    
    @Test
    public void test_validate_returnsTrueAndHasNoErrors()
    {
        callRestriction = getPopulatedCallRestriction();
        assertTrue(callRestriction.validate());
        assertFalse(callRestriction.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        callRestriction = getPopulatedCallRestriction();
        CallRestriction copy = callRestriction.copy(false);

        assertNull(copy.getId());
        assertEquals((Integer)0, copy.getVersion());
        assertEquals(callRestriction.getDestination(), copy.getDestination());
        assertEquals(callRestriction.getForEveryone(), copy.getForEveryone());
        assertEquals(callRestriction.getSubscriber(), copy.getSubscriber());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithIdAndVersion()
    {
        callRestriction = getPopulatedCallRestriction();
        CallRestriction copy = callRestriction.copy(true);

        assertEquals(callRestriction.getId(), copy.getId());
        assertEquals(callRestriction.getVersion(), copy.getVersion());
        assertEquals(callRestriction.getDestination(), copy.getDestination());
        assertEquals(callRestriction.getForEveryone(), copy.getForEveryone());
        assertEquals(callRestriction.getSubscriber(), copy.getSubscriber());
    }
    
    private CallRestriction getPopulatedCallRestriction()
    {
        CallRestriction cr = new CallRestriction();
        cr.setDestination(String.valueOf(System.currentTimeMillis()));
        cr.setForEveryone(Boolean.FALSE);
        cr.setSubscriber(new Subscriber());
        cr.setId(System.currentTimeMillis());
        cr.setVersion(10);
        return cr;
    }
}
