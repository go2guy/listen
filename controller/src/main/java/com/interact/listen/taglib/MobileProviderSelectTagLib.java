package com.interact.listen.taglib;

import com.interact.listen.EmailerUtil;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class MobileProviderSelectTagLib extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String withEmpty = "false";

    @Override
    public int doStartTag() throws JspException
    {
        JspWriter out = pageContext.getOut();

        try
        {
            out.println("<select" + (id != null ? " id=\"" + id + "\"" : "") + ">");
            if(Boolean.valueOf(withEmpty))
            {
                out.println("  <option value=\"N/A\">N/A</option>");
            }
            for(EmailerUtil.SmsEmailAddress entry : EmailerUtil.SmsEmailAddress.values())
            {
                out.println("  <option value=\"" + entry.getEmailAddress() + "\">" + entry.getProvider() + "</option>");
            }
            out.println("</select>");
        }
        catch(IOException e)
        {
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setWithEmpty(String withEmpty)
    {
        this.withEmpty = withEmpty;
    }

    public String getWithEmpty()
    {
        return withEmpty;
    }
}
