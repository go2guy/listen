package com.interact.listen.api.util;

import static org.junit.Assert.assertNotNull;

import com.interact.listen.ListenTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class HttpDateTest extends ListenTest
{
    private SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(HttpDate.class, "Cannot instantiate utility class HttpDate");
    }

    @Test
    public void test_now_returnsCorrectlyFormattedDate() throws ParseException
    {
        // this test doesn't actually validate that the date is close to now; only validates the format is valid
        format.setLenient(false);

        String now = HttpDate.now();
        Date parsed = format.parse(now); // if exception thrown, the test will fail (which is one of our assertions)
        assertNotNull(parsed);
    }

    @Test
    public void test_parse_canParseFormattedDateWithoutException() throws ParseException
    {
        String date = format.format(new Date());
        Date parsed = HttpDate.parse(date); // if exception thrown, the test will fail (which is one of our assertions)
        assertNotNull(parsed);
    }
}
