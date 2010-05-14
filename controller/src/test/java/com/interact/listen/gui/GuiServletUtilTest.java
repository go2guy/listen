package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class GuiServletUtilTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        Constructor<?> constructor = GuiServletUtil.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        try
        {
            constructor.newInstance();
            fail("Expected InvocationTargetException with root cause of AssertionError for utility class constructor");
        }
        catch(InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AssertionError);
            assertEquals("Cannot instantiate utility class GuiServletUtil", cause.getMessage());
        }
    }
}
