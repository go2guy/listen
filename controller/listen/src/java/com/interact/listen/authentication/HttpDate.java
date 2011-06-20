package com.interact.listen.authentication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class HttpDate
{
    public static final String FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private HttpDate()
    {
        throw new AssertionError("Cannot instantiate utility class HttpDate");
    }

    public static String now()
    {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        Date date = new Date();
        return format.format(date);
    }

    public static Date parse(String date) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(FORMAT);
        return format.parse(date);
    }
}
