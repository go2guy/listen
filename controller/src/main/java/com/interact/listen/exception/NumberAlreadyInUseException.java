package com.interact.listen.exception;

public class NumberAlreadyInUseException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final String number;

    public NumberAlreadyInUseException(String number)
    {
        super("Number [" + number + "] already in use");
        this.number = number;
    }

    public String getNumber()
    {
        return number;
    }
}
