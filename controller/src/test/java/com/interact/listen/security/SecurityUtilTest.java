package com.interact.listen.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class SecurityUtilTest
{
    @Test
    public void test_hashPassword_returnsBase64EncodedSHA1password()
    {
        final String plaintext = "super";
        final String sha1base64 = "hFG6ihTXl1PTTLM7UbpGtLAl64E=";

        assertEquals(sha1base64, SecurityUtil.hashPassword(plaintext));
    }

    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        Constructor<?> constructor = SecurityUtil.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        try
        {
            constructor.newInstance();
            fail("Expected InvocationTargetException with root cause of AssertionError for utility class constructor");
        }
        catch(InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AssertionError);
            assertEquals("Cannot instantiate utility class SecurityUtil", cause.getMessage());
        }
    }
}
