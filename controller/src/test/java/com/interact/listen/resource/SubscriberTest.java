package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubscriberTest
{
    private Subscriber subscriber;

    @Before
    public void setUp()
    {
        subscriber = new Subscriber();
    }

    @After
    public void tearDown()
    {
        subscriber = null;
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        subscriber.setNumber(number);

        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        subscriber.setId(id);

        assertEquals(id, subscriber.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        subscriber.setVersion(version);

        assertEquals(version, subscriber.getVersion());
    }

    @Test
    public void test_toXml_withDeepFalseAndIdSet_returnsXml()
    {
        final Long id = System.currentTimeMillis();
        subscriber.setId(id);

        final String expected = "<subscriber href=\"/subscribers/" + id + "\"/>";
        assertEquals(expected, subscriber.toXml(false));
    }

    @Test
    public void test_toXml_withDeepTrueAndPropertiesSet_returnsXml()
    {
        final Long id = System.currentTimeMillis();
        final String number = String.valueOf(System.currentTimeMillis()) + "foo";
        subscriber.setId(id);
        subscriber.setNumber(number);

        // TODO get rid of the hard-coded xml and use a real parser for comparison (so order doesn't matter)
        final String expected = "<subscriber href=\"/subscribers/" + id + "\"><id>" + id + "</id><number>" + number +
                                "</number></subscriber>";
        assertEquals(expected, subscriber.toXml(true));
    }

    @Test
    public void test_loadFromXml_withValidXmlAndLoadIdTrue_populatesSubscriber()
    {
        final Long id = System.currentTimeMillis();
        final String number = String.valueOf(System.currentTimeMillis()) + "foo";

        final String xml = "<subscriber href=\"/subscribers/" + id + "\"><id>" + id + "</id><number>" + number +
                           "</number></subscriber>";
        subscriber.loadFromXml(xml, true);
        assertEquals(id, subscriber.getId());
        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_loadFromXml_withValidXmlAndLoadIdFalse_populatesSubscriberWithNullId()
    {
        final Long id = System.currentTimeMillis();
        final String number = String.valueOf(System.currentTimeMillis()) + "foo";

        final String xml = "<subscriber href=\"/subscribers/" + id + "\"><id>" + id + "</id><number>" + number +
                           "</number></subscriber>";
        subscriber.loadFromXml(xml, false);
        assertNull(subscriber.getId());
        assertEquals(number, subscriber.getNumber());
    }

    @Test
    public void test_loadFromXml_withXmlMissingIdAndLoadIdTrue_populatesSubscriberWithNullId()
    {
        final Long id = System.currentTimeMillis();
        final String number = String.valueOf(System.currentTimeMillis()) + "foo";

        final String xml = "<subscriber href=\"/subscribers/" + id + "\"><number>" + number + "</number></subscriber>";
        subscriber.loadFromXml(xml, true);
        assertNull(subscriber.getId());
        assertEquals(number, subscriber.getNumber());
    }
    
    @Test
    public void test_loadFromXml_withXmlMissingNumber_populatesSubscriberWithNullNumber()
    {
        final Long id = System.currentTimeMillis();
        final String number = String.valueOf(System.currentTimeMillis()) + "foo";

        final String xml = "<subscriber href=\"/subscribers/" + id + "\"><id>" + id + "</id></subscriber>";
        subscriber.loadFromXml(xml, true);
        assertEquals(id, subscriber.getId());
        assertNull(subscriber.getNumber());
    }
}
