package com.interact.listen.resource;

import static org.junit.Assert.*;

import com.interact.listen.history.Channel;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class HistoryTest
{
    private History history;

    @Before
    public void setUp()
    {
        history = new History();
    }

    @Test
    public void test_dateCreated_defaultsToNewDate()
    {
        assertNotNull(history.getDateCreated());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        history.setId(id);
        assertEquals(id, history.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        history.setVersion(version);
        assertEquals(version, history.getVersion());
    }

    @Test
    public void test_setPerformedBySubscriber_withValidSubscriber_setsPerformedBySubscriber()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        history.setPerformedBySubscriber(subscriber);
        assertEquals(subscriber, history.getPerformedBySubscriber());
    }

    @Test
    public void test_setOnSubscriber_withValidSubscriber_setsOnSubscriber()
    {
        final Subscriber subscriber = new Subscriber();
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));
        history.setOnSubscriber(subscriber);
        assertEquals(subscriber, history.getOnSubscriber());
    }

    @Test
    public void test_setAction_withValidAction_setsAction()
    {
        final String action = String.valueOf(System.currentTimeMillis());
        history.setAction(action);
        assertEquals(action, history.getAction());
    }

    @Test
    public void test_setChannel_withValidChannel_setsChannel()
    {
        final Channel channel = Channel.GUI;
        history.setChannel(channel);
        assertEquals(channel, history.getChannel());
    }

    @Test
    public void test_setDescription_withValidDescription_setsDescription()
    {
        final String description = String.valueOf(System.currentTimeMillis());
        history.setDescription(description);
        assertEquals(description, history.getDescription());
    }

    @Test
    public void test_setDateCreated_withValidDateCreated_setsDateCreated()
    {
        final Date dateCreated = new Date();
        history.setDateCreated(dateCreated);
        assertEquals(dateCreated, history.getDateCreated());
    }

    @Test
    public void test_validate_withValidProperties_returnsTrueAndHasNoErrors()
    {
        history = getPopulatedHistory();
        assertTrue(history.validate());
        assertFalse(history.hasErrors());
    }

    @Test
    public void test_validate_nullAction_returnsFalseWithErrors()
    {
        history = getPopulatedHistory();
        history.setAction(null);
        assertFalse(history.validate());
        assertTrue(history.hasErrors());
    }

    @Test
    public void test_validate_blankAction_returnsFalseWithErrors()
    {
        history = getPopulatedHistory();
        history.setAction(" ");
        assertFalse(history.validate());
        assertTrue(history.hasErrors());
    }

    @Test
    public void test_validate_nullDescription_returnsFalseWithErrors()
    {
        history = getPopulatedHistory();
        history.setDescription(null);
        assertFalse(history.validate());
        assertTrue(history.hasErrors());
    }

    @Test
    public void test_validate_blankDescription_returnsFalseWithErrors()
    {
        history = getPopulatedHistory();
        history.setDescription(" ");
        assertFalse(history.validate());
        assertTrue(history.hasErrors());
    }

    @Test
    public void test_validate_nullDateCreated_returnsFalseWithErrors()
    {
        history = getPopulatedHistory();
        history.setDateCreated(null);
        assertFalse(history.validate());
        assertTrue(history.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        history = getPopulatedHistory();
        History copy = history.copy(false);

        assertNull(copy.getId());
        assertEquals((Integer)0, copy.getVersion());
        assertEquals(history.getAction(), copy.getAction());
        assertEquals(history.getChannel(), copy.getChannel());
        assertEquals(history.getDateCreated(), copy.getDateCreated());
        assertEquals(history.getDescription(), copy.getDescription());
        assertEquals(history.getOnSubscriber(), copy.getOnSubscriber());
        assertEquals(history.getPerformedBySubscriber(), copy.getPerformedBySubscriber());
    }

    @Test
    public void test_copy_withIdAndVersion_createsCopyWithoutIdAndVersion()
    {
        history = getPopulatedHistory();
        History copy = history.copy(true);

        assertEquals(history.getId(), copy.getId());
        assertEquals(history.getVersion(), copy.getVersion());
        assertEquals(history.getAction(), copy.getAction());
        assertEquals(history.getChannel(), copy.getChannel());
        assertEquals(history.getDateCreated(), copy.getDateCreated());
        assertEquals(history.getDescription(), copy.getDescription());
        assertEquals(history.getOnSubscriber(), copy.getOnSubscriber());
        assertEquals(history.getPerformedBySubscriber(), copy.getPerformedBySubscriber());
    }

    @Test
    public void test_equals_withRelevantPropertiesEqual_returnsTrue()
    {
        history.setAction(String.valueOf(System.currentTimeMillis()));
        history.setDateCreated(new Date());
        history.setDescription(String.valueOf(System.currentTimeMillis()));

        // set relevant properties equal
        History that = new History();
        that.setAction(history.getAction());
        that.setDateCreated(history.getDateCreated());
        that.setDescription(history.getDescription());

        // set an irrelevant property to something different
        history.setChannel(Channel.TUI);
        that.setChannel(Channel.GUI);

        assertTrue(history.equals(that));
    }

    @Test
    public void test_equals_differentAction_returnsFalse()
    {
        history.setAction(String.valueOf(System.currentTimeMillis()));
        history.setDateCreated(new Date());
        history.setDescription(String.valueOf(System.currentTimeMillis()));

        // set relevant properties equal
        History that = new History();
        that.setAction("");
        that.setDateCreated(history.getDateCreated());
        that.setDescription(history.getDescription());

        // set an irrelevant property to something different
        history.setChannel(Channel.TUI);
        that.setChannel(Channel.GUI);

        assertFalse(history.equals(that));
    }

    @Test
    public void test_equals_differentDateCreated_returnsFalse()
    {
        history.setAction(String.valueOf(System.currentTimeMillis()));
        history.setDateCreated(new Date());
        history.setDescription(String.valueOf(System.currentTimeMillis()));

        // set relevant properties equal
        History that = new History();
        that.setAction(history.getAction());
        that.setDateCreated(null);
        that.setDescription(history.getDescription());

        // set an irrelevant property to something different
        history.setChannel(Channel.TUI);
        that.setChannel(Channel.GUI);

        assertFalse(history.equals(that));
    }

    @Test
    public void test_equals_differentDescription_returnsFalse()
    {
        history.setAction(String.valueOf(System.currentTimeMillis()));
        history.setDateCreated(new Date());
        history.setDescription(String.valueOf(System.currentTimeMillis()));

        // set relevant properties equal
        History that = new History();
        that.setAction(history.getAction());
        that.setDateCreated(history.getDateCreated());
        that.setDescription("");

        // set an irrelevant property to something different
        history.setChannel(Channel.TUI);
        that.setChannel(Channel.GUI);

        assertFalse(history.equals(that));
    }

    @Test
    public void test_hashCode()
    {
        history = new History();
        history.setAction("1234");
        history.setDateCreated(new Date(12341234));
        history.setDescription("4321");
        assertEquals(1491766221, history.hashCode());
    }
    
    private History getPopulatedHistory()
    {
        History h = new History();
        h.setAction(String.valueOf(System.currentTimeMillis()));
        h.setChannel(Channel.GUI);
        h.setDescription(String.valueOf(System.currentTimeMillis()));
        h.setOnSubscriber(new Subscriber());
        h.setPerformedBySubscriber(new Subscriber());
        h.setVersion(10);
        h.setId(System.currentTimeMillis());
        return h;
    }
}
