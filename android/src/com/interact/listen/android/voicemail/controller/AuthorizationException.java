package com.interact.listen.android.voicemail.controller;

public class AuthorizationException extends Exception
{
    private final String api;

    public AuthorizationException(String api)
    {
        super();
        this.api = api;
    }
    
    public AuthorizationException()
    {
        super();
        this.api = "";
    }

    public String getApi()
    {
        return api;
    }
}