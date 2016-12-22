package com.interact.listen.exceptions;

/**
 * Exception from an attempt to export call records to csv
 */
public class ListenExportException extends Exception
{
    public ListenExportException(String string)
    {
        super(string);
    }
}
