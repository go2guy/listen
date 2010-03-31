package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class Iso8601DateConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new Iso8601DateConverter();
    }

    @Test
    public void test_marshal_withDate_returnsIso8601DateString()
    {
        final Date date = new Date();
        final String expected = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT).format(date);

        assertEquals(expected, converter.marshal(date));
    }

    @Test
    public void test_unmarshal_withValidDateString_returnsDate() throws ConversionException
    {
        final Date date = new Date();
        final String toUnmarshal = new SimpleDateFormat(Iso8601DateConverter.ISO8601_FORMAT).format(date);

        assertEquals(date, converter.unmarshal(toUnmarshal));
    }

    @Test
    public void test_unmarshal_withInvalidDateString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "brillig";

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
