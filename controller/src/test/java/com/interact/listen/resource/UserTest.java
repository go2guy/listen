package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
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

    @After
    public void tearDown()
    {
        user = null;
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
    public void test_validate_validProperties_returnsTrue()
    {
        user = getPopulatedUser();
        user.validate();
        
        assertFalse(user.hasErrors());
    }
    
    @Test
    public void test_validate_nullSubscriber_returnsFalse()
    {
        user = getPopulatedUser();
        user.setSubscriber(null);
        user.validate();
        
        assertTrue(user.hasErrors());
    }
    
    @Test
    public void test_validate_nullUsername_returnsFalse()
    {
        user = getPopulatedUser();
        user.setUsername(null);
        user.validate();
        
        assertTrue(user.hasErrors());
    }
    
    @Test
    public void test_validate_nullPassword_returnsFalse()
    {
        user = getPopulatedUser();
        user.setPassword(null);
        user.validate();
        
        assertTrue(user.hasErrors());
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
