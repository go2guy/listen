package com.interact.listen.spot;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpotCommunicationExceptionTest
{
    @Test
    public void test_construct_setsMessage()
    {
        final String message = "It is currently 3:49am, and I am in the office. Wunderbar.";
        Exception e = new SpotCommunicationException(message);
        assertEquals(message, e.getMessage());
    }
}
