package com.interact.listen.license;

public class NotLicensedException extends Exception
{
    private static final long serialVersionUID = 1L;

    public NotLicensedException(ListenFeature feature)
    {
        super("Feature " + feature.toString() + " is not licensed");
    }
}
