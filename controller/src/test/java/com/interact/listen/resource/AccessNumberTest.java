package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;

import org.junit.Before;
import org.junit.Test;

public class AccessNumberTest extends ListenTest
{
    private AccessNumber accessNumber;

    @Before
    public void setUp()
    {
        accessNumber = new AccessNumber();
    }

    @Test
    public void test_setId_setsId()
    {
        final Long id = System.currentTimeMillis();
        accessNumber.setId(id);
        assertEquals(id, accessNumber.getId());
    }

    @Test
    public void test_setVersion_setsVersion()
    {
        final Integer version = 1234;
        accessNumber.setVersion(version);
        assertEquals(version, accessNumber.getVersion());
    }

    @Test
    public void test_setNumber_setsNumber()
    {
        final String number = TestUtil.randomString();
        accessNumber.setNumber(number);
        assertEquals(number, accessNumber.getNumber());
    }

    @Test
    public void test_setSubscriber_setsSubscriber()
    {
        final Subscriber subscriber = createSubscriber(session);
        accessNumber.setSubscriber(subscriber);
        assertEquals(subscriber, accessNumber.getSubscriber());
    }

    @Test
    public void test_setGreetingLocation_setsGreetingLocation()
    {
        final String greetingLocation = TestUtil.randomString();
        accessNumber.setGreetingLocation(greetingLocation);
        assertEquals(greetingLocation, accessNumber.getGreetingLocation());
    }

    @Test
    public void test_setSupportsMessageLight_setsSupportsMessageLight()
    {
        final Boolean light = Boolean.TRUE;
        accessNumber.setSupportsMessageLight(light);
        assertEquals(light, accessNumber.getSupportsMessageLight());
    }

    @Test
    public void test_validate_nullNumber_returnsFalse()
    {
        accessNumber.setNumber(null);
        accessNumber.setSubscriber(new Subscriber());
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("number cannot be null or empty"));
    }

    @Test
    public void test_validate_blankNumber_returnsFalse()
    {
        accessNumber.setNumber(" ");
        accessNumber.setSubscriber(new Subscriber());
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("number cannot be null or empty"));
    }

    @Test
    public void test_validate_nullSubscriber_returnsFalse()
    {
        accessNumber.setNumber(TestUtil.randomString());
        accessNumber.setSubscriber(null);
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("subscriber cannot be null"));
    }
}
