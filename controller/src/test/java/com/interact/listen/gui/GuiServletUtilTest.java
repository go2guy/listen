package com.interact.listen.gui;

import com.interact.listen.ListenTest;

import org.junit.Test;

public class GuiServletUtilTest extends ListenTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(GuiServletUtil.class, "Cannot instantiate utility class GuiServletUtil");
    }
}
