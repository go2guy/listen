package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SubscriberTest
{
    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = 1234L;
        Subscriber subscriber = new Subscriber();
        subscriber.setId(1234L);

        assertEquals(id, subscriber.getId());
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = "54321";
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(number);

        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_setId_withValidId_setsHref()
    {
        final Long id = 43132412L;
        Subscriber subscriber = new Subscriber();
        subscriber.setId(id);

        assertEquals("/subscribers/" + id, subscriber.getHref());
    }

    @Test
    public void test_toXml_withPopulatedSubscriber_returnsCorrectXml()
    {
        final Long id = Long.valueOf(System.currentTimeMillis());
        final String number = String.valueOf(System.currentTimeMillis());

        Subscriber subscriber = new Subscriber();
        subscriber.setId(id);
        subscriber.setNumber(number);

        StringBuilder expected = new StringBuilder();
        expected.append("<subscriber href=\"/subscribers/").append(id).append("\">").append("\n");
        expected.append("  <id>").append(id).append("</id>").append("\n");
        expected.append("  <number>").append(number).append("</number>").append("\n");
        expected.append("</subscriber>");

        assertEquals(expected.toString(), subscriber.toXml());
    }
}
