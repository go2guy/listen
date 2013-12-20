package com.interact.listen.spot;

public class SpotCommunicationException extends Exception
{
    private static final long serialVersionUID = 1L;
    private int errorCode;

    public SpotCommunicationException(String message)
    {
        super(message);
    }

    public SpotCommunicationException(String message, int errorCode)
    {
        super(message);
        this.errorCode = errorCode;
    }
}
