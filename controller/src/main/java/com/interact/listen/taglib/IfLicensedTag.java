package com.interact.listen.taglib;

import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class IfLicensedTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String feature;

    @Override
    public int doStartTag() throws JspException
    {
        if(License.isLicensed(ListenFeature.valueOf(feature)))
        {
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    public String getFeature()
    {
        return feature;
    }

    public void setFeature(String feature)
    {
        this.feature = feature;
    }
}
