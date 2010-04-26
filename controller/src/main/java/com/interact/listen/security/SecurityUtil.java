package com.interact.listen.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public final class SecurityUtil
{
    private SecurityUtil()
    {
        throw new AssertionError("Cannot instantiate utility class SecurityUtil");
    }

    public static String hashPassword(String plaintextPassword)
    {
        return new String(Base64.encodeBase64(DigestUtils.sha(plaintextPassword)));
    }
}
