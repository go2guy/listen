package com.interact.listen.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class ResourceTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    @Override
    public int doStartTag() throws JspException
    {
        final String context = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        JspWriter out = pageContext.getOut();

        try
        {
            out.print(context + path);
        }
        catch(IOException e)
        {
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }
}
