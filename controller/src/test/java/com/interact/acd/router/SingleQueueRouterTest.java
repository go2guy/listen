package com.interact.acd.router;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

public class SingleQueueRouterTest
{
    @Test
    public void test_addDestination_addsDestinationThatCanBeRemoved()
    {
        Destination destination = mock(Destination.class);
        Router router = new SingleQueueRouter();
        router.addDestination(destination);
        assertTrue(router.removeDestination(destination));
    }

    @Test
    public void test_removeDestination_whenDestinationWasNotAdded_returnsFalse()
    {
        Destination destination = mock(Destination.class);
        Router router = new SingleQueueRouter();
        assertFalse(router.removeDestination(destination));
    }

    @Test
    public void test_route_withOneParcelAndOneAvailableDestination_parcelRoutedToDestination()
        throws InterruptedException, RouteUnsuccessfulException, ExecutionException
    {
        Parcel parcel = mock(Parcel.class);
        Destination destination = mock(Destination.class);

        // create a router with a single available destination and route a single parcel
        Router router = new SingleQueueRouter();
        router.addDestination(destination);
        Future<Boolean> future = router.route(parcel);

        while(!future.isDone())
        {
            Thread.sleep(100);
        }

        verify(parcel).routeTo(destination);
        assertTrue(future.get());
    }

    @Test
    public void test_route_twoParcelsTwoDestinations_parcelsGoToDestinationsInOrder() throws InterruptedException,
        RouteUnsuccessfulException, ExecutionException
    {
        // mock out a couple of parcels and destinations
        Parcel parcel0 = mock(Parcel.class);
        Parcel parcel1 = mock(Parcel.class);
        Destination dest0 = mock(Destination.class);
        Destination dest1 = mock(Destination.class);

        // create a router with two available destinations
        Router router = new SingleQueueRouter();
        router.addDestination(dest1);
        router.addDestination(dest0);

        // try to route two parcels
        Future<Boolean> future0 = router.route(parcel0);
        Future<Boolean> future1 = router.route(parcel1);

        // wait for both parcels to be routed
        while(!future0.isDone() || !future1.isDone())
        {
            Thread.sleep(100);
        }

        verify(parcel0).routeTo(dest1);
        verify(parcel1).routeTo(dest0);

        // make sure destinations have been removed automatically
        // removeDestination will return false if they don't exist
        assertFalse(router.removeDestination(dest0));
        assertFalse(router.removeDestination(dest1));
        assertTrue(future0.get());
        assertTrue(future1.get());
    }

    @Test
    public void test_route_parcelAddedWithNoDestinations_waitsUntilDestinationAndThenRoutes()
        throws InterruptedException, RouteUnsuccessfulException, ExecutionException
    {
        // create a router without a destination and try to route a parcel with it
        Parcel parcel = mock(Parcel.class);
        Router router = new SingleQueueRouter();
        Future<Boolean> future = router.route(parcel);

        Thread.sleep(1000);
        assertFalse(future.isDone()); // parcel should still be queued, there weren't any available destinations

        // now add a destination to allow the parcel to be routed
        Destination destination = mock(Destination.class);
        router.addDestination(destination);

        while(!future.isDone())
        {
            Thread.sleep(100);
        }

        verify(parcel).routeTo(destination);
        assertFalse(router.removeDestination(destination));
        assertTrue(future.get());
    }

    @Test
    public void test_route_whenRouteToThrowsException_exceptionIsAvailableFromFuture() throws InterruptedException
    {
        final String message = String.valueOf(System.currentTimeMillis());

        // create a parcel stub that just throws an exception
        Parcel parcel = new Parcel()
        {
            @Override
            public void routeTo(Destination destination) throws RouteUnsuccessfulException
            {
                throw new RouteUnsuccessfulException(message);
            }
        };

        // create a router with a single available destination
        Destination destination = mock(Destination.class);
        Router router = new SingleQueueRouter();
        router.addDestination(destination);

        // call route() and wait
        Future<Boolean> future = router.route(parcel);
        while(!future.isDone()) // wait for task to finish
        {
            Thread.sleep(100);
        }

        try
        {
            future.get();
            fail("Expected ExecutionException");
        }
        catch(ExecutionException e)
        {
            Throwable cause = e.getCause();
            assertEquals(RouteUnsuccessfulException.class, cause.getClass());
            assertEquals(message, cause.getMessage());
        }
    }
}
