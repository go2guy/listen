package com.interact.listen.util;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenTest;

import org.joda.time.Duration;
import org.junit.Test;

public class DateUtilTest extends ListenTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(DateUtil.class, "Cannot instantiate utility class DateUtil");
    }

    @Test
    public void test_printDuration_withLessThanOneMinute_printsDuration()
    {
        Duration duration = new Duration(1000 * 34);
        assertEquals("0:34", DateUtil.printDuration(duration));
    }

    @Test
    public void test_printDuration_withMoreThanOneMinute_printsDuration()
    {
        Duration duration = new Duration(1000 * 75);
        assertEquals("1:15", DateUtil.printDuration(duration));
    }

    @Test
    public void test_printDuration_withLessThanTenSeconds_printsDuration()
    {
        Duration duration = new Duration(1000 * 4);
        assertEquals("0:04", DateUtil.printDuration(duration));
    }

    @Test
    public void test_roundUpToNearestSecond_withX499ms_roundsUp()
    {
        testRoundUpToNearestSecond(1499, 2000);
    }

    @Test
    public void test_roundUpToNearestSecond_withX001ms_roundsUp()
    {
        testRoundUpToNearestSecond(1001, 2000);
    }

    @Test
    public void test_roundUpToNearestSecond_withX999ms_roundsUp()
    {
        testRoundUpToNearestSecond(1999, 2000);
    }

    @Test
    public void test_roundUpToNearestSecond_withX000ms_keepsSameValue()
    {
        testRoundUpToNearestSecond(1000, 1000);
    }

    private void testRoundUpToNearestSecond(long toRound, long expected)
    {
        assertEquals(new Duration(expected), DateUtil.roundUpToNearestSecond(new Duration(toRound)));
    }
}
