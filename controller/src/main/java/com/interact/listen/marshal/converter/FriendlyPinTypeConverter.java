package com.interact.listen.marshal.converter;

import com.interact.listen.resource.Pin.PinType;

/**
 * Converts a {@link PinType} to a human-friendly format.
 */
public class FriendlyPinTypeConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        PinType type = (PinType)value;
        return type.toFriendlyName();
    }

    @Override
    public Object unmarshal(String value)
    {
        return PinType.fromFriendlyName(value);
    }
}
