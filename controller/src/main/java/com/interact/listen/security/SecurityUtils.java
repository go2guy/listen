package com.interact.listen.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public final class SecurityUtils
{
    private SecurityUtils()
    {
        throw new AssertionError("Cannot instantiate utility class SecurityUtils");
    }

    public static String hashPassword(String plaintextPassword)
    {
        return new String(Base64.encodeBase64(DigestUtils.sha(plaintextPassword)));
    }
}
