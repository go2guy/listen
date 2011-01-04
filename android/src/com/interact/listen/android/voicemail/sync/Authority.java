package com.interact.listen.android.voicemail.sync;

public enum Authority
{
    VOICEMAIL("com.interact.listen.voicemail"),
    CONTACTS("com.android.contacts");
    
    private final String authority;
    
    private Authority(String authority)
    {
        this.authority = authority;
    }
    
    public String get()
    {
        return authority;
    }

    private static final String[] AUTHORITIES;
    static
    {
        Authority[] auths = Authority.values();
        AUTHORITIES = new String[auths.length];
        for(int i = 0; i < auths.length; ++i)
        {
            AUTHORITIES[i] = auths[i].get();
        }
    }

    public static String[] getAuthorities()
    {
        return AUTHORITIES.clone();
    }
    
    public static Authority getByAuthority(String authority)
    {
        Authority[] auths = Authority.values();
        for(int i = 0; i < auths.length; ++i)
        {
            if(auths[i].get().equals(authority))
            {
                return auths[i];
            }
        }
        return null;
    }
}
