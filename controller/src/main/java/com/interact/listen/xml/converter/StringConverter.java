package com.interact.listen.xml.converter;

public class StringConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        return (String)value;
    }

    @Override
    public Object unmarshal(String value)
    {
        return value;
    }
}
