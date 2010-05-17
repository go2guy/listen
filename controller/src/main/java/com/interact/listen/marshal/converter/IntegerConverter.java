package com.interact.listen.marshal.converter;

/**
 * Converts an {@code Integer}.
 */
public class IntegerConverter implements Converter
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
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e)
        {
            throw new ConversionException(value, "Integer");
        }
    }
}
