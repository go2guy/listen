package com.interact.listen.resource;

import static org.junit.Assert.*;

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

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), subscriber.getVersion());
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        subscriber.setNumber(number);

        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_setVoicemailPin_withValidVoicemailPin_setsVoicemailPin()
    {
        final String pin = String.valueOf(System.currentTimeMillis());
        subscriber.setVoicemailPin(pin);

        assertEquals(pin, subscriber.getVoicemailPin());
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
    public void test_validate_validProperties_returnsNoErrors()
    {
        subscriber = getPopulatedSubscriber();

        assertTrue(subscriber.validate());
        assertFalse(subscriber.hasErrors());
    }

    @Test
    public void test_validate_nullNumber_returnsHasErrors()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber(null);

        assertFalse(subscriber.validate());
        assertTrue(subscriber.hasErrors());
    }

    @Test
    public void test_validate_blankVoicemailGreetingLocation_returnsNoErrors()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setVoicemailGreetingLocation("");

        assertTrue(subscriber.validate());
        assertFalse(subscriber.hasErrors());
    }

    @Test
    public void test_validate_nullVoicemailGreetingLocation_returnsNoErrors()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setVoicemailGreetingLocation(null);

        assertTrue(subscriber.validate());
        assertFalse(subscriber.hasErrors());
    }

    @Test
    public void test_validate_blankNumber_returnsHasErrors()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber("");

        assertFalse(subscriber.validate());
        assertTrue(subscriber.hasErrors());
    }

    @Test
    public void test_validate_whitespaceNumber_returnsHasErrors()
    {
        subscriber = getPopulatedSubscriber();
        subscriber.setNumber(" ");

        assertFalse(subscriber.validate());
        assertTrue(subscriber.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Subscriber original = getPopulatedSubscriber();
        Subscriber copy = original.copy(false);

        assertEquals(original.getNumber(), copy.getNumber());
        assertEquals(original.getVoicemailGreetingLocation(), copy.getVoicemailGreetingLocation());
        assertEquals(original.getVoicemailPin(), copy.getVoicemailPin());
        assertTrue(original.getVoicemails() == copy.getVoicemails()); // same reference

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        Subscriber original = getPopulatedSubscriber();
        Subscriber copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(subscriber.equals(subscriber));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(subscriber.equals(null));
    }

    @Test
    public void test_equals_thatNotAsubscriber_returnsFalse()
    {
        assertFalse(subscriber.equals(new String()));
    }

    @Test
    public void test_equals_numberNotEqual_returnsFalse()
    {
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));

        Subscriber that = new Subscriber();
        that.setNumber(null);

        assertFalse(subscriber.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String number = String.valueOf(System.currentTimeMillis());

        subscriber.setNumber(number);

        Subscriber that = new Subscriber();
        that.setNumber(number);

        assertTrue(subscriber.equals(that));
    }

    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);
        s.setNumber(String.valueOf(System.currentTimeMillis()));
        s.setVoicemailGreetingLocation("foo/bar/baz/biz");
        return s;
    }
}
