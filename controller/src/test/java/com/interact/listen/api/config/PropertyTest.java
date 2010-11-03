package com.interact.listen.api.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenTest;
import com.interact.listen.config.Property;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class PropertyTest extends ListenTest
{
    @Test
    public void test_delimitedStringToSet_withEmptyString_returnsEmptySet()
    {
        Set<String> set = Property.delimitedStringToSet("", ",");
        assertEquals(0, set.size());
    }

    @Test
    public void test_delimitedStringToSet_withEmptyStringOnBothSidesOfDelimiter_returnsEmptySet()
    {
        Set<String> set = Property.delimitedStringToSet(",", ",");
        assertEquals(0, set.size());
    }

    @Test
    public void test_delimitedStringToSet_withOneValueAndTrailingDelimiter_returnsSetWithOneElement()
    {
        Set<String> set = Property.delimitedStringToSet("foo,", ",");
        assertEquals(1, set.size());
        assertTrue(set.contains("foo"));
    }

    @Test
    public void test_delimitedStringToSet_withOneValueAndLeadingDelimiter_returnsSetWithOneElement()
    {
        Set<String> set = Property.delimitedStringToSet(",foo", ",");
        assertEquals(1, set.size());
        assertTrue(set.contains("foo"));
    }

    @Test
    public void test_delimitedStringToSet_withTwoValuesFormattedNotStupidly_returnsSetWithTwoElements()
    {
        Set<String> set = Property.delimitedStringToSet("foo,bar", ",");
        assertEquals(2, set.size());
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
    }

    @Test
    public void test_delimitedStringToSet_withNullString_returnsEmptySet()
    {
        Set<String> set = Property.delimitedStringToSet(null, ",");
        assertNotNull(set);
        assertEquals(0, set.size());
    }

    @Test
    public void test_setToDelimitedString_withEmptySet_returnsEmptyString()
    {
        String string = Property.setToDelimitedString(new HashSet<String>(), ",");
        assertEquals("", string);
    }

    @Test
    public void test_setToDelimitedString_withSetContainingOneElement_returnsNonDelimitedStringWithElement()
    {
        Set<String> set = new HashSet<String>();
        set.add("foo");
        String string = Property.setToDelimitedString(set, ",");
        assertEquals("foo", string);
    }

    @Test
    public void test_setToDelimitedString_withSetContainingTwoElements_returnsDelimitedSet()
    {
        Set<String> set = new HashSet<String>();
        set.add("foo");
        set.add("bar");
        String string = Property.setToDelimitedString(set, ",");
        assertTrue(string.equals("foo,bar") || string.equals("bar,foo"));
    }
}
