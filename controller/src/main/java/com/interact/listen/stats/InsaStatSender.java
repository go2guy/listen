package com.interact.listen.stats;

import com.interact.insa.client.StatsPublisher;

public class InsaStatSender implements StatSender
{
    @Override
    public void send(Stat stat)
    {
        try
        {
            StatsPublisher.send(stat.getStatId(), 1, StatsPublisher.INCREMENT);
        }
        catch(Exception e)
        {
            // catch general Exception, we don't want to fail the whole request just because we can't write a stat
            System.out.println("Error writing stat");
            e.printStackTrace();
        }
    }
}
