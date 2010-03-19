package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubscriberTest
{
    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(number);
        assertEquals(number, subscriber.getNumber());
    }
}
