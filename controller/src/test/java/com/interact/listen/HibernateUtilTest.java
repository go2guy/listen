package com.interact.listen;

import org.junit.Test;

public class HibernateUtilTest extends ListenTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(HibernateUtil.class, "Cannot instantiate utility class HibernateUtil");
    }
}
