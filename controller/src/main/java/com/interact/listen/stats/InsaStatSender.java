package com.interact.listen.stats;

import com.interact.insa.client.StatsPublisher;

public class InsaStatSender implements StatSender
{
    @Override
    public void send(Stat stat)
    {
        send(stat, null);
    }
    
    @Override
    public void send(Stat stat, Long value)
    {
        try
        {
            StatsPublisher.send(stat.getStatId(), value == null ? 1 : value, value == null ? StatsPublisher.INCREMENT
                                                                                          : StatsPublisher.SET);
        }
        catch(Exception e)
        {
            // catch general Exception, we don't want to fail the whole request just because we can't write a stat
            System.out.println("Error writing stat");
            e.printStackTrace();
        }
    }
}
