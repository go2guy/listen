package com.interact.listen.exception;

public class UnauthorizedModificationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public UnauthorizedModificationException(String message)
    {
        super(message);
    }
}
