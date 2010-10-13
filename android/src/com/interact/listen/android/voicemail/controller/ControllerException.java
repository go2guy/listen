package com.interact.listen.android.voicemail.controller;

public class ControllerException extends Exception
{
    private final String message;

    public ControllerException(String message)
    {
        this.message = message;
    }

    public ControllerException(Throwable cause)
    {
        super(cause);
        this.message = "Error connecting to server";
    }

    public String getMessage()
    {
        return message;
    }
}
