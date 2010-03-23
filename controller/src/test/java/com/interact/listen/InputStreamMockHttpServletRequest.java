package com.interact.listen;

import javax.servlet.ServletInputStream;

import org.springframework.mock.web.MockHttpServletRequest;

public class InputStreamMockHttpServletRequest extends MockHttpServletRequest
{
    private ServletInputStream inputStream;

    public ServletInputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(ServletInputStream inputStream)
    {
        this.inputStream = inputStream;
    }
}
