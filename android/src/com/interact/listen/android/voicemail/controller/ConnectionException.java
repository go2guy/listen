package com.interact.listen.android.voicemail.controller;

public class ConnectionException extends Exception
{
    private final String api;

    public ConnectionException(Throwable cause, String api)
    {
        super(cause);
        this.api = api;
    }

    public String getApi()
    {
        return api;
    }
}
