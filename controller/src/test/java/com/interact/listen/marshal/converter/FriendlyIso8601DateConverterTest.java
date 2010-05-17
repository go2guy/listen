package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class FriendlyIso8601DateConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new FriendlyIso8601DateConverter();
    }

    @Test
    public void test_marshal_withDate_returnsIso8601DateString()
    {
        final Date date = new Date();
        final String expected = new SimpleDateFormat(FriendlyIso8601DateConverter.ISO8601_FORMAT).format(date);

        assertEquals(expected, converter.marshal(date));
    }

    @Test
    public void test_unmarshal_withValidDateString_returnsDate() throws ConversionException
    {
        Date date = new Date();
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0);
        date = new Date(c.getTimeInMillis());
        
        final String toUnmarshal = new SimpleDateFormat(FriendlyIso8601DateConverter.ISO8601_FORMAT).format(date);

        assertEquals(date, converter.unmarshal(toUnmarshal));
    }

    @Test
    public void test_unmarshal_withInvalidDateString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "slithy";

        try
        {
            converter.unmarshal(toUnmarshal);
            fail("Expected ConversionException for unparseable date");
        }
        catch(ConversionException e)
        {
            assertEquals("Could not convert value [" + toUnmarshal + "] to type [Date]", e.getMessage());
        }
    }
}
