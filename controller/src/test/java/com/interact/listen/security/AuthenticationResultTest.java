package com.interact.listen.security;

import static org.junit.Assert.*;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationResultTest extends ListenTest
{
    private AuthenticationResult result;

    @Before
    public void setUp()
    {
        result = new AuthenticationResult();
    }

    @Test
    public void test_isSuccessful_defaultsToFalse()
    {
        assertFalse(result.isSuccessful());
    }

    @Test
    public void test_getGroups_defaultsToEmptySet()
    {
        assertEquals(0, result.getGroups().size());
    }

    @Test
    public void test_getDisplayName_defaultsToEmptyString()
    {
        assertEquals("", result.getDisplayName());
    }

    @Test
    public void test_getTelephoneNumber_defaultsToNull()
    {
        assertNull(result.getTelephoneNumber());
    }

    @Test
    public void test_setGroups_setsGroups()
    {
        Set<String> groups = new HashSet<String>();
        groups.add(TestUtil.randomString());
        result.setGroups(groups);
        assertEquals(groups, result.getGroups());
    }

    @Test
    public void test_setSuccessful_setsSuccessful()
    {
        result.setSuccessful(true);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void test_setDisplayName_setsDisplayName()
    {
        String displayName = TestUtil.randomString();
        result.setDisplayName(displayName);
        assertEquals(displayName, result.getDisplayName());
    }

    @Test
    public void test_setTelephoneNumber_setsTelephoneNumber()
    {
        String number = String.valueOf(TestUtil.randomNumeric(8));
        result.setTelephoneNumber(number);
        assertEquals(number, result.getTelephoneNumber());
    }

    @Test
    public void test_toString_returnsPrettyString()
    {
        boolean successful = true;
        String displayName = TestUtil.randomString();
        String telephoneNumber = TestUtil.randomString();
        Set<String> groups = new HashSet<String>();
        groups.add(TestUtil.randomString());

        result.setDisplayName(displayName);
        result.setGroups(groups);
        result.setSuccessful(successful);
        result.setTelephoneNumber(telephoneNumber);

        String expected = "successful = [" + successful + "], ";
        expected += "displayName = [" + displayName + "], ";
        expected += "telephoneNumber = [" + telephoneNumber + "], ";
        expected += "mail = [null], ";
        expected += "groups = [" + Arrays.toString(groups.toArray()) + "]";
        assertEquals(expected, result.toString());
    }
}
