package com.interact.listen.security;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;

import org.junit.Test;

public class AuthenticationExceptionTest extends ListenTest
{
    @Test
    public void test_constructWithString_setsMessage()
    {
        final String message = TestUtil.randomString();
        Exception e = new AuthenticationException(message);
        assertEquals(message, e.getMessage());
    }

    @Test
    public void test_constructWithThrowable_setsRootCause()
    {
        Throwable cause = new Throwable();
        Exception e = new AuthenticationException(cause);
        assertEquals(cause, e.getCause());
    }
}
