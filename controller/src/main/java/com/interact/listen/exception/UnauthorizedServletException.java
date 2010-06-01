package com.interact.listen.exception;

import javax.servlet.http.HttpServletResponse;

public class UnauthorizedServletException extends ListenServletException
{
    private static final long serialVersionUID = 1L;

    public UnauthorizedServletException()
    {
        super(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
    }

    public UnauthorizedServletException(String reason)
    {
        super(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - " + reason, "text/plain");
    }
}
