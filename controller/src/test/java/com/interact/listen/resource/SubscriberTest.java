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
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Subscriber original = getPopulatedSubscriber();
        Subscriber copy = original.copy(false);

        assertEquals(original.getUsername(), copy.getUsername());
        assertEquals(original.getPassword(), copy.getPassword());
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
    public void test_equals_usernameNotEqual_returnsFalse()
    {
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));

        Subscriber that = new Subscriber();
        that.setUsername(null);

        assertFalse(subscriber.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String username = String.valueOf(System.currentTimeMillis());

        subscriber.setUsername(username);

        Subscriber that = new Subscriber();
        that.setUsername(username);

        assertTrue(subscriber.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashcodeForRelevantFields()
    {
        Subscriber obj = new Subscriber();

        // hashcode-relevant properties set to static values for predictability
        obj.setUsername("JJBLT");

        // set a property that has no effect on hashcode to something dynamic
        obj.setPassword(String.valueOf(System.currentTimeMillis()));

        assertEquals(70610985, obj.hashCode());
    }

    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setPassword(String.valueOf(System.currentTimeMillis()));
        s.setUsername(String.valueOf(System.currentTimeMillis()));
        s.setVersion(1);
        return s;
    }
}
