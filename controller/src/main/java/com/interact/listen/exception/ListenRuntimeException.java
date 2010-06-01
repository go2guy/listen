package com.interact.listen.exception;

public class ListenRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public ListenRuntimeException(Throwable t)
    {
        super(t);
    }
}
