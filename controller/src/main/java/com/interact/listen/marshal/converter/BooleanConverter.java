package com.interact.listen.marshal.converter;

/**
 * Converts a {@code Boolean}.
 */
public class BooleanConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        return Boolean.toString((Boolean)value);
    }

    @Override
    public Object unmarshal(String value)
    {
        return Boolean.valueOf(value);
    }
}
