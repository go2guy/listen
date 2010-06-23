package com.interact.listen.marshal.converter;

import com.interact.listen.resource.CallDetailRecord.CallDirection;

/**
 * Converts a {@link CallDirection}.
 */
public class CallDirectionConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        CallDirection direction = (CallDirection)value;
        return direction.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return CallDirection.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "CallDirection");
        }
    }
}
