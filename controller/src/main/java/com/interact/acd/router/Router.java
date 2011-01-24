package com.interact.acd.router;

import java.util.concurrent.Future;

/**
 * Routes {@link Parcel}s to {@link Destination}s.
 */
public interface Router
{
    /**
     * Route the provided {@link Parcel} to the next available {@link Destination}. Destinations are added using
     * {@link #addDestination(Destination)}. If no {@code Destination}s are available, the {@code Router} implementation
     * <i>may</i> block until one becomes available.
     * <p>
     * <b>Developer note</b>: After the {@code Parcel} is routed, the {@code Destination} to which it was routed is
     * removed from the {@code Router}. It is the responsibility of the client code using the {@code Router} to re-add
     * the {@code Destination} using {@link #addDestination(Destination)} once the destination becomes available again.
     * <p>
     * This method returns a {@link Future} that can be used to access the routing result, if desired. The {@code
     * Future} provides a {@code Boolean} return value indicating whether or not the routing succeeded or failed. The
     * {@code Future}'s {@link Future#cancel(boolean)} may also be used to cancel the routing.
     * 
     * @param parcel {@code Parcel} to route
     */
    public Future<Boolean> route(Parcel parcel);

    /**
     * Adds the provided {@link Destination} so that {@link Parcel}s that have been added using {@link #route(Parcel)}
     * may be routed to it.
     * 
     * @param destination {@code Destination} to add
     */
    public void addDestination(Destination destination);

    /**
     * Removes the provided {@link Destination} from the router. Once removed, {@link Parcel}s provided to
     * {@link #route(Parcel)} will no longer be routed to the {@code Destination}.
     * 
     * @param destination {@code Destination} to remove
     * @return {@code true} if the {@code Destination} was found and remvoed, {@code false} if the destination was not
     * found
     */
    public boolean removeDestination(Destination destination);
}
