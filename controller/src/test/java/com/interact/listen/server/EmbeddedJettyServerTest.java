package com.interact.listen.server;

import com.interact.listen.ListenTest;

import org.junit.Test;

public class EmbeddedJettyServerTest extends ListenTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(EmbeddedJettyServer.class,
                                              "Cannot instantiate main() class EmbeddedJettyServer");
    }
}
