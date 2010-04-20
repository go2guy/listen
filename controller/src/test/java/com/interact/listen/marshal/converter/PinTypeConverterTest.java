package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.resource.Pin.PinType;

import org.junit.Before;
import org.junit.Test;

public class PinTypeConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new PinTypeConverter();
    }

    @Test
    public void test_marshal_withActivePinType_returnsPinTypeString()
    {
        assertEquals("ACTIVE", converter.marshal(PinType.ACTIVE));
    }

    @Test
    public void test_marshal_withAdminPinType_returnsPinTypeString()
    {
        assertEquals("ADMIN", converter.marshal(PinType.ADMIN));
    }

    @Test
    public void test_marshal_withPassivePinType_returnsPinTypeString()
    {
        assertEquals("PASSIVE", converter.marshal(PinType.PASSIVE));
    }

    @Test
    public void test_unmarshal_withActiveString_returnsActivePinType() throws ConversionException
    {
        assertEquals(PinType.ACTIVE, converter.unmarshal("ACTIVE"));
    }

    @Test
    public void test_unmarshal_withAdminString_returnsAdminPinType() throws ConversionException
    {
        assertEquals(PinType.ADMIN, converter.unmarshal("ADMIN"));
    }

    @Test
    public void test_unmarshal_withPassiveString_returnsPassivePinType() throws ConversionException
    {
        assertEquals(PinType.PASSIVE, converter.unmarshal("PASSIVE"));
    }

    @Test
    public void test_unmarshal_withUnrecognizedString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "RUHROH!";
        try
        {
            converter.unmarshal(toUnmarshal);
            fail("Expected ConversionException for unrecognized PinType");
        }
        catch(ConversionException e)
        {
            assertEquals("Could not convert value [" + toUnmarshal + "] to type [PinType]", e.getMessage());
        }
    }
}
