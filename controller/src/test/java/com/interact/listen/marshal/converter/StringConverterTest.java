package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class StringConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new StringConverter();
    }

    @Test
    public void test_marshal_withString_returnsString()
    {
        final String string = "benjaminsisko";
        assertEquals(string, converter.marshal(string));
    }

    @Test
    public void test_unmarshal_withString_returnsString() throws ConversionException
    {
        final String string = "pinballwizard";
        assertEquals(string, converter.unmarshal(string));
    }
}
