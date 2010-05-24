package com.interact.listen.license;

import com.interact.listen.ListenServletException;

import javax.servlet.http.HttpServletResponse;

public class NotLicensedException extends ListenServletException
{
    private static final long serialVersionUID = 1L;

    public NotLicensedException(ListenFeature feature)
    {
        super(HttpServletResponse.SC_NOT_IMPLEMENTED, "Feature " + feature.toString() + " is not licensed",
              "text/plain");
    }
}
