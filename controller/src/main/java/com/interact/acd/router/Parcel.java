package com.interact.acd.router;

/**
 * A parcel that can be routed to a particular destination. All implementations <b>must</b> implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()} to allow {@link Router} implementations to effectively
 * work with the {@code Parcel}s in collections.
 */
public interface Parcel
{
    public void routeTo(Destination destination) throws RouteUnsuccessfulException;
}
