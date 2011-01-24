package com.interact.acd.router;

/**
 * A destination to which a {@link Parcel} can be routed. All implementations <b>must</b> implement
 * {@link Object#equals(Object)} and {@link Object#hashCode()} to allow {@link Router} implementations to effectively
 * work with the {@code Destination}s in collections.
 */
public interface Destination
{
    public String getAddress();
}
