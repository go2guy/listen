package com.interact.listen.marshal.converter;

public class ConversionException extends Exception
{
    private static final long serialVersionUID = 1L;

    public ConversionException(String value, String type)
    {
        super("Could not convert value [" + value + "] to type [" + type + "]");
    }
}
