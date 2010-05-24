package com.interact.listen;

import javax.servlet.ServletException;

public class ListenServletException extends ServletException
{
    private static final long serialVersionUID = 1L;

    private final int status;
    private final String content;
    private final String contentType;

    public ListenServletException(int status)
    {
        this.status = status;
        this.content = "";
        this.contentType = "";
    }

    public ListenServletException(int status, String content, String contentType)
    {
        this.status = status;
        this.content = content;
        this.contentType = contentType;
    }

    public int getStatus()
    {
        return status;
    }

    public String getContent()
    {
        return content;
    }

    public String getContentType()
    {
        return contentType;
    }
}
