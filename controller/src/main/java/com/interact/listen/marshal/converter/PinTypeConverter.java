package com.interact.listen.marshal.converter;

import com.interact.listen.resource.Pin.PinType;

public class PinTypeConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        PinType type = (PinType)value;
        return type.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return PinType.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "PinType");
        }
    }
}
