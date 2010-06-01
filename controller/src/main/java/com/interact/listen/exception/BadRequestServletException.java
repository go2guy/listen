package com.interact.listen.exception;

import javax.servlet.http.HttpServletResponse;

public class BadRequestServletException extends ListenServletException
{
    private static final long serialVersionUID = 1L;

    public BadRequestServletException(String message)
    {
        super(HttpServletResponse.SC_BAD_REQUEST, message, "text/plain");
    }
}
