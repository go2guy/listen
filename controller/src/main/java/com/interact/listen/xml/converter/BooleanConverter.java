package com.interact.listen.xml.converter;

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
