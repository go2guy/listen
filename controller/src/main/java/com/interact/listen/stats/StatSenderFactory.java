package com.interact.listen.stats;

public final class StatSenderFactory
{
    private StatSenderFactory()
    {
        throw new AssertionError();
    }
    
    public static StatSender getStatSender()
    {
        return new InsaStatSender();
    }
}
