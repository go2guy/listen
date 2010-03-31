package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class IntegerConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new IntegerConverter();
    }

    @Test
    public void test_marshal_withInteger_returnsCorrectString()
    {
        final Integer toMarshal = Integer.valueOf(4283);
        assertEquals("4283", converter.marshal(toMarshal));
    }

    @Test
    public void test_unmarshal_withIntegerString_returnsCorrectInteger() throws ConversionException
    {
        final String toUnmarshal = "431234";
        assertEquals(Integer.valueOf(toUnmarshal), converter.unmarshal(toUnmarshal));
    }

    @Test
    public void test_unmarshal_withUnparseableString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "notparseable";

        try
        {
            converter.unmarshal(toUnmarshal);
            fail("Expected ConversionException for unparseable Integer");
        }
        catch(ConversionException e)
        {
            assertEquals("Could not convert value [" + toUnmarshal + "] to type [Integer]", e.getMessage());
        }
    }
}
