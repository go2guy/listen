package com.interact.listen.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
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
    
    @Test
    public void test_toJoda_withEpoch_returnsCorrectLocalDateTime() throws ParseException
    {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2010-11-03 15:13:32.435");
        LocalDateTime joda = DateUtil.toJoda(date);

        assertEquals(new LocalDateTime(2010, 11, 3, 15, 13, 32, 435), joda);
    }
    
    @Test
    public void test_toJoda_withDateOneDayAgo_returnsDurationOfOneDay()
    {
        // NOTE: i'm using a whole day for this test since it's tough for us to get down to second precision
        // if we could control what the system time is when the method is called, it'd be a different story...
        Date oneDayAgo = new LocalDateTime().minusDays(1).toDateTime().toCalendar(Locale.getDefault()).getTime();
        Duration duration = DateUtil.durationSinceDate(oneDayAgo);

        // tests allow 500ms tolerance either way
        assertTrue(duration.isLongerThan(new Duration((1000 * 60 * 60 * 24) - 500)));
        assertTrue(duration.isShorterThan(new Duration((1000 * 60 * 60 * 24) + 500)));
    }
}
