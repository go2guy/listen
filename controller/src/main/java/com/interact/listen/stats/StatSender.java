package com.interact.listen.stats;

public interface StatSender
{
    /**
     * Sends the provided {@link Stat} to a collector using the "INCREMENT" operation with value of one. It is
     * recommended that implementations of this method do not throw {@code Exception}s.
     * 
     * @param stat {@code Stat} to send
     */
    public void send(Stat stat);

    /**
     * Sends the provided {@link Stat} to a collector using the "SET" operation with the provided value. It is
     * recommended that implementations of this method do not throw {@code Exception}s.
     * 
     * @param stat {@code Stat} to send
     * @param value value to set
     */
    public void send(Stat stat, Long value);
}
