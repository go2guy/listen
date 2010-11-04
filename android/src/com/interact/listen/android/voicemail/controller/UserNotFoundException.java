package com.interact.listen.android.voicemail.controller;

public class UserNotFoundException extends Exception
{
    private final String username;

    public UserNotFoundException(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }
}
