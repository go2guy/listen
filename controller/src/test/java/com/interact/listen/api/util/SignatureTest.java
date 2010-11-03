package com.interact.listen.api.util;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenTest;

import javax.servlet.ServletException;

import org.junit.Test;

public class SignatureTest extends ListenTest
{
    @Test
    public void test_constructor_throwsAssertionErrorWithMessage() throws IllegalAccessException,
        InstantiationException
    {
        assertConstructorThrowsAssertionError(Signature.class, "Cannot instantiate utility class Signature");
    }

    @Test
    public void test_create_createsBase64EncodedSignature() throws ServletException
    {
        String date = "Tue, 02 Nov 2010 14:11:53 CDT";
        String signature = Signature.create(date);
        assertEquals("6ST+IHAfV2NoWTyPrHgk4s5/7DM=", signature);
    }
}
