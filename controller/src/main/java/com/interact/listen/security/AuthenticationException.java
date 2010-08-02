package com.interact.listen.security;

public class AuthenticationException extends Exception
{
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message)
    {
        super(message);
    }

    public AuthenticationException(Throwable cause)
    {
        super(cause);
    }
}
