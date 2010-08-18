package com.interact.listen.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenTest;

import org.junit.Test;

public class ComparisonUtilTest extends ListenTest
{
    private static final String NON_NULL_OBJECT1 = "foo";
    private static final String NON_NULL_OBJECT2 = "bar";

    @Test
    public void test_isEqual_bothNull_returnsTrue()
    {
        assertTrue(ComparisonUtil.isEqual(null, null));
    }

    @Test
    public void test_isEqual_firstNullSecondNot_returnsFalse()
    {
        assertFalse(ComparisonUtil.isEqual(null, NON_NULL_OBJECT1));
    }

    @Test
    public void test_isEqual_secondNullFirstNot_returnsFalse()
    {
        assertFalse(ComparisonUtil.isEqual(NON_NULL_OBJECT1, null));
    }

    @Test
    public void test_isEqual_objectsEqual_returnsTrue()
    {
        assertTrue(ComparisonUtil.isEqual(NON_NULL_OBJECT1, NON_NULL_OBJECT1));
    }

    @Test
    public void test_isEquals_objectsNotEqual_returnsFalse()
    {
        assertFalse(ComparisonUtil.isEqual(NON_NULL_OBJECT1, NON_NULL_OBJECT2));
    }

    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(ComparisonUtil.class, "Cannot instantiate utility class ComparisonUtil");
    }
}
