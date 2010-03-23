package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubscriberTest
{
    private Subscriber subscriber;

    @Before
    public void setUp()
    {
        subscriber = new Subscriber();
    }

    @After
    public void tearDown()
    {
        subscriber = null;
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        subscriber.setNumber(number);

        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        subscriber.setId(id);

        assertEquals(id, subscriber.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        subscriber.setVersion(version);

        assertEquals(version, subscriber.getVersion());
    }
}
