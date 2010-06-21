package com.interact.listen;

import com.interact.listen.history.Channel;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;

import org.hibernate.Session;

/**
 * Wrapper service for performing operations that persist {@link Resource}s. This allows for invoking {@code Resource}
 * callback methods (e.g. {@link Resource#afterSave(Session)} on persistence operations.
 */
public class PersistenceService
{
    private Session session;
    private Subscriber currentSubscriber;
    private Channel channel;

    public PersistenceService(Session session, Subscriber currentSubscriber, Channel channel)
    {
        if(session == null)
        {
            throw new IllegalArgumentException("Cannot construct PersistenceService with null session");
        }

        if(channel == null)
        {
            throw new IllegalArgumentException("Cannot construct PersistenceService with null channel");
        }

        this.channel = channel;
        this.currentSubscriber = currentSubscriber;
        this.session = session;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public Subscriber getCurrentSubscriber()
    {
        return currentSubscriber;
    }

    public Session getSession()
    {
        return session;
    }

    public Resource get(Class<? extends Resource> resourceClass, Long id)
    {
        return (Resource)session.get(resourceClass, id);
    }

    public Long save(Resource resource)
    {
        resource.beforeSave(this);
        Long id = (Long)session.save(resource);
        resource.afterSave(this);

        return id;
    }

    public void update(Resource updatedResource, Resource originalResource)
    {
        updatedResource.beforeUpdate(this, originalResource);
        session.update(updatedResource);
        updatedResource.afterUpdate(this, originalResource);
    }

    public void delete(Resource resource)
    {
        resource.beforeDelete(this);
        session.delete(resource);
        resource.afterDelete(this);
    }
}
