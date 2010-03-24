package com.interact.listen.marshal;

public class MarshallerNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;

    public MarshallerNotFoundException(String contentType)
    {
        super("Marshaller not found for content type [" + contentType + "]");
    }
}
