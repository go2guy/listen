package com.interact.listen.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComparisonUtilTest
{
    private static final String NON_NULL_OBJECT = "foo";

    @Test
    public void test_isEqual_bothNull_returnsTrue()
    {
        assertTrue(ComparisonUtil.isEqual(null, null));
    }

    @Test
    public void test_isEqual_firstNullSecondNot_returnsFalse()
    {
        assertFalse(ComparisonUtil.isEqual(null, NON_NULL_OBJECT));
    }

    @Test
    public void test_isEqual_secondNullFirstNot_returnsFalse()
    {
        assertFalse(ComparisonUtil.isEqual(NON_NULL_OBJECT, null));
    }

    @Test
    public void test_isEqual_objectsEqual_returnsTrue()
    {
        assertTrue(ComparisonUtil.isEqual(NON_NULL_OBJECT, NON_NULL_OBJECT));
    }
}
