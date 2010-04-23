package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UserTest
{
    private User user;

    @Before
    public void setUp()
    {
        user = new User();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), user.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        user.setId(id);

        assertEquals(id, user.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        user.setVersion(version);

        assertEquals(version, user.getVersion());
    }

    @Test
    public void test_validate_validProperties_returnsNoErrors()
    {
        user = getPopulatedUser();

        assertTrue(user.validate());
        assertFalse(user.hasErrors());
    }

    @Test
    public void test_validate_nullSubscriber_returnsHasErrors()
    {
        user = getPopulatedUser();
        user.setSubscriber(null);

        assertFalse(user.validate());
        assertTrue(user.hasErrors());
    }

    @Test
    public void test_validate_nullUsername_returnsHasErrors()
    {
        user = getPopulatedUser();
        user.setUsername(null);

        assertFalse(user.validate());
        assertTrue(user.hasErrors());
    }

    @Test
    public void test_validate_nullPassword_returnsHasErrors()
    {
        user = getPopulatedUser();
        user.setPassword(null);

        assertFalse(user.validate());
        assertTrue(user.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        User original = getPopulatedUser();
        User copy = original.copy(false);

        assertEquals(original.getPassword(), copy.getPassword());
        assertTrue(original.getSubscriber() == copy.getSubscriber()); // same reference
        assertEquals(original.getUsername(), copy.getUsername());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        User original = getPopulatedUser();
        User copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    private User getPopulatedUser()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setVersion(1);
        s.setNumber(String.valueOf(System.currentTimeMillis()));
        s.setVoicemailGreetingLocation("foo/bar/baz/biz");

        User u = new User();
        u.setSubscriber(s);
        u.setUsername("foo");
        u.setPassword("bar");

        return u;
    }
}
