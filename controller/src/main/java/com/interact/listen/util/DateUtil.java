package com.interact.listen.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

public final class DateUtil
{
    private DateUtil()
    {
        throw new AssertionError("Cannot instantiate utility class DateUtil");
    }

    public static String printDuration(Duration duration)
    {
        Duration d = roundUpToNearestSecond(duration);
        double s = Math.floor(d.getMillis() / 1000.0);
        return String.format("%01.0f:%02.0f", s < 60 ? 0 : s / 60, s % 60);
    }

    public static Duration roundUpToNearestSecond(Duration duration)
    {
        return new Duration(((duration.getMillis() + 500) / 1000) * 1000);
    }

    public static LocalDateTime toJoda(Date date)
    {
        return LocalDateTime.fromDateFields(date);
    }

    public static Duration durationSinceDate(Date date)
    {
        LocalDateTime local = toJoda(date);
        return new Duration(local.toDateTime(), new DateTime());
    }
}
