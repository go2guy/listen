package com.interact.acd.router;

import java.util.concurrent.*;

import org.joda.time.LocalDateTime;

/**
 * An implementation of {@link Router} that uses a single {@link Parcel} queue to route parcels to {@link Destination}s.
 */
public class SingleQueueRouter implements Router
{
    private final BlockingQueue<Destination> destinations;
    private final PriorityBlockingQueue<Runnable> parcels;
    private final ExecutorService executor;

    /**
     * Constructs a new {@code SingleQueueRouter}.
     */
    public SingleQueueRouter()
    {
        destinations = new LinkedBlockingQueue<Destination>();

        // use a priority blocking queue:
        // - priority: so we can order entries by the time they were received, older ones first
        // - blocking: so the executor will block if there are no entries in the queue, and grab
        // the next one once it gets queued
        parcels = new PriorityBlockingQueue<Runnable>();

        // executor with core/max size of 1 that has infinite keepalive
        executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, parcels);
    }

    @Override
    public Future<Boolean> route(Parcel parcel)
    {
        Callable<Boolean> task = new RouteTask(parcel, new LocalDateTime());
        return executor.submit(task);
    }

    @Override
    public void addDestination(Destination destination)
    {
        destinations.add(destination);
    }

    @Override
    public boolean removeDestination(Destination destination)
    {
        // assumes the Destination implementation has implemented equals and hashCode
        return destinations.remove(destination);
    }

    /**
     * {@code Callable} passed to the executor that, when executed, routes the task's parcel to the next available
     * {@code Destination}. Implements {@link Comparable} so that the {@code PriorityBlockingQueue} for the executor can
     * sort tasks and give higher priority to tasks that arrive earlier.
     * <p>
     * Note: this class has a natural ordering that is inconsistent with equals
     */
    private class RouteTask implements Callable<Boolean>, Comparable<RouteTask>
    {
        private final Parcel parcel;
        private final LocalDateTime received;

        /**
         * Creates a new {@code RouteTask} with the provided parcel and time received.
         * 
         * @param parcel {@link Parcel} to route
         * @param received time the parcel was received; older parcels receive higher priority and are executed sooner
         */
        public RouteTask(Parcel parcel, LocalDateTime received)
        {
            this.parcel = parcel;
            this.received = received;
        }

        @Override
        @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS", justification = "Comparison only matters for the PriorityBlockingQueue, which uses a specific ordering")
        public int compareTo(RouteTask other)
        {
            return received.compareTo(other.received);
        }

        @Override
        public Boolean call()
        {
            try
            {
                Destination destination = destinations.take(); // blocks until there's a destination available
                parcel.routeTo(destination);
                // this doesn't re-add the destination; it's the responsibility of the client to do that when the
                // destination is available again
                return Boolean.TRUE;
            }
            catch(RouteUnsuccessfulException e)
            {
                return Boolean.FALSE;
            }
            catch(InterruptedException e)
            {
                return Boolean.FALSE;
            }
        }
    }
}
