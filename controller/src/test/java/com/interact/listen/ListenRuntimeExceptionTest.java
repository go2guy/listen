package com.interact.listen;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ListenRuntimeExceptionTest
{
    @Test
    public void test_construct_setsThrowable()
    {
        Exception cause = new Exception();
        Exception exception = new ListenRuntimeException(cause);
        assertEquals(cause, exception.getCause());
    }
}
