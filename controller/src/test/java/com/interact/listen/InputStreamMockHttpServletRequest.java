package com.interact.listen;

import javax.servlet.ServletInputStream;

import org.springframework.mock.web.MockHttpServletRequest;

public class InputStreamMockHttpServletRequest extends MockHttpServletRequest
{
    private ServletInputStream inputStream;

    public InputStreamMockHttpServletRequest()
    {
        super();
        this.setAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY, new StringBuilder());
        this.setAttribute(OutputBufferFilter.OUTPUT_TYPE_KEY, "text/plain");
    }

    public ServletInputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(ServletInputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public String getOutputBufferString()
    {
        return ((StringBuilder)this.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY)).toString();
    }

    public String getOutputBufferType()
    {
        return (String)this.getAttribute(OutputBufferFilter.OUTPUT_TYPE_KEY);
    }
}
