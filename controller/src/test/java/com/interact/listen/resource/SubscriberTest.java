package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    @Test
    public void test_validate_nullNumber_returnsFalse()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber(null);
        assertFalse(subscriber.validate());
    }

    @Test
    public void test_validate_blankNumber_returnsFalse()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber("");
        assertFalse(subscriber.validate());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsFalse()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber(" ");
        assertFalse(subscriber.validate());
    }

    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);
        s.setNumber(String.valueOf(System.currentTimeMillis()));
        return s;
    }
}
