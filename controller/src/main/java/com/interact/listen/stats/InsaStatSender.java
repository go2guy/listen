package com.interact.listen.stats;

import com.interact.insa.client.StatsPublisher;
import com.interact.insa.client.StatsPublisher.Operator;

import org.apache.log4j.Logger;

/**
 * Sends statistic information to INSA systems.
 */
public class InsaStatSender implements StatSender
{
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(InsaStatSender.class);

    private static final String STAT_SOURCE = "LISTEN";

    static
    {
        StatsPublisher.setSource(STAT_SOURCE);
    }

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
            Operator statOperation = value == null ? Operator.INCREMENT : Operator.SET;
            Long statValue = value == null ? 1 : value;
            StatsPublisher.send(stat.getStatId(), statValue, statOperation);
        }
        catch(Exception e)
        {
            // catch general Exception, we don't want to fail the whole request just because we can't write a stat
            LOG.error("Error writing stat", e);
        }
    }
}
