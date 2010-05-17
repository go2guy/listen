package com.interact.listen.marshal.converter;

/**
 * Converts a {@code Long}.
 */
public class LongConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        return String.valueOf(value);
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Long.parseLong(value);
        }
        catch(NumberFormatException e)
        {
            throw new ConversionException(value, "Long");
        }
    }
}
