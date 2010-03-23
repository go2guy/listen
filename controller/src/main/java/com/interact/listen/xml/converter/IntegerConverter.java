package com.interact.listen.xml.converter;

public class IntegerConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        return String.valueOf(value);
    }

    @Override
    public Object unmarshal(String value)
    {
        return Integer.parseInt(value);
    }
}
