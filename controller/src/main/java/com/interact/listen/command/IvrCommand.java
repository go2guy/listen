package com.interact.listen.command;

import org.hibernate.Session;

/**
 * Interface representing a mechanism for converting an {@code Action} to JSON.
 */
public interface IvrCommand
{
    /**
     * Returns the class as a JSON {@code String}.
     * 
     * @return {@code String} JSON representation of provided value
     */
    public String toIvrCommandJson(Session session);
}
