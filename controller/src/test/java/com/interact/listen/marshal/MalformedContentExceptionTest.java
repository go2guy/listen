package com.interact.listen.marshal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MalformedContentExceptionTest
{
    @Test
    public void test_construct_withThrowable_setsParentCause()
    {
        Throwable cause = new Throwable("Rhymenocerous vs. Hip-hoppapotamus");
        Exception e = new MalformedContentException(cause);

        assertEquals(cause, e.getCause());
    }

    @Test
    public void test_construct_withMessage_setsMessage()
    {
        final String message = "Parliament Funkadelic";
        Exception e = new MalformedContentException(message);

        assertEquals(message, e.getMessage());
    }
}
