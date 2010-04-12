package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ListenSpotSubscriberTest
{
    private ListenSpotSubscriber listenSpotSubscriber;

    @Before
    public void setUp()
    {
        listenSpotSubscriber = new ListenSpotSubscriber();
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        listenSpotSubscriber.setId(id);

        assertEquals(id, listenSpotSubscriber.getId());
    }

    @Test
    public void test_setHttpApi_withValidHttpApi_setsId()
    {
        final String httpApi = "http://example.com/spot/ccxml/basichttp";
        listenSpotSubscriber.setHttpApi(httpApi);

        assertEquals(httpApi, listenSpotSubscriber.getHttpApi());
    }

    @Test
    public void test_validate_nullHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi(null);

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }

    @Test
    public void test_validate_blankHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi("");

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }

    @Test
    public void test_validate_whitespaceHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi(" ");

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }
}
