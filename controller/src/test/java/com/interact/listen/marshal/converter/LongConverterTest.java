package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class LongConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new LongConverter();
    }

    @Test
    public void test_marshal_withLong_returnsCorrectString()
    {
        final Long toMarshal = Long.valueOf(4283);
        assertEquals("4283", converter.marshal(toMarshal));
    }

    @Test
    public void test_unmarshal_withLongString_returnsCorrectLong() throws ConversionException
    {
        final String toUnmarshal = "431234";
        assertEquals(Long.valueOf(toUnmarshal), converter.unmarshal(toUnmarshal));
    }

    @Test
    public void test_unmarshal_withUnparseableString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "notparseable";

        try
        {
            converter.unmarshal(toUnmarshal);
            fail("Expected ConversionException for unparseable Long");
        }
        catch(ConversionException e)
        {
            assertEquals("Could not convert value [" + toUnmarshal + "] to type [Long]", e.getMessage());
        }
    }
}
