package com.interact.acd.router;

public class RouteUnsuccessfulException extends Exception
{
    private static final long serialVersionUID = 1L; // FIXME

    public RouteUnsuccessfulException(String message)
    {
        super(message);
    }

    public RouteUnsuccessfulException(Throwable cause)
    {
        super(cause);
    }
}
