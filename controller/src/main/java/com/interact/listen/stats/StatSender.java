package com.interact.listen.stats;

public interface StatSender
{
    /**
     * Sends the provided {@link Stat} to a collector. It is recommended that implementations of this method do not
     * throw {@code Exception}s.
     * 
     * @param stat {@code Stat} to send
     */
    public void send(Stat stat);
}
