package com.interact.listen.stats;

import com.interact.insa.client.StatsPublisher;
import com.interact.insa.client.StatsPublisher.Operator;

import org.apache.log4j.Logger;

public class InsaStatSender implements StatSender
{
    private static final Logger LOG = Logger.getLogger(InsaStatSender.class);

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
            StatsPublisher.send(stat.getStatId(), value == null ? 1 : value, value == null ? Operator.INCREMENT
                                                                                          : Operator.SET);
        }
        catch(Exception e)
        {
            // catch general Exception, we don't want to fail the whole request just because we can't write a stat
            LOG.error("Error writing stat", e);
        }
    }
}
