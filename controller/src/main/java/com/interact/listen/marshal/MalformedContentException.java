package com.interact.listen.marshal;

public class MalformedContentException extends Exception
{
    private static final long serialVersionUID = 1L;

    public MalformedContentException(Throwable t)
    {
        super(t);
    }

    public MalformedContentException(String message)
    {
        super(message);
    }
}
