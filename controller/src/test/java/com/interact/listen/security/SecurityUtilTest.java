package com.interact.listen.security;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenTest;

import org.junit.Test;

public class SecurityUtilTest extends ListenTest
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
        assertConstructorThrowsAssertionError(SecurityUtil.class, "Cannot instantiate utility class SecurityUtil");
    }
}
