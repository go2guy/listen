package com.interact.listen;

import com.interact.listen.history.Channel;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;

import org.hibernate.Session;

/**
 * Wrapper service for performing operations that persist {@link Resource}s. This allows for invoking {@code Resource}
 * callback methods (e.g. {@link Resource#afterSave(Session)} on persistence operations.
 */
public class DefaultPersistenceService implements PersistenceService
{
    private Session session;
    private Subscriber currentSubscriber;
    private Channel channel;
    private HistoryService historyService = new DefaultHistoryService(this);

    public DefaultPersistenceService(Session session, Subscriber currentSubscriber, Channel channel)
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

    @Override
    public Channel getChannel()
    {
        return channel;
    }

    @Override
    public Subscriber getCurrentSubscriber()
    {
        return currentSubscriber;
    }

    @Override
    public Session getSession()
    {
        return session;
    }

    @Override
    public Resource get(Class<? extends Resource> resourceClass, Long id)
    {
        return (Resource)session.get(resourceClass, id);
    }

    @Override
    public Long save(Resource resource)
    {
        resource.beforeSave(this, historyService);
        Long id = (Long)session.save(resource);
        resource.afterSave(this, historyService);

        return id;
    }

    @Override
    public void update(Resource updatedResource, Resource originalResource)
    {
        updatedResource.beforeUpdate(this, historyService, originalResource);
        session.update(updatedResource);
        updatedResource.afterUpdate(this, historyService, originalResource);
    }

    @Override
    public void delete(Resource resource)
    {
        resource.beforeDelete(this, historyService);
        session.delete(resource);
        resource.afterDelete(this, historyService);
    }
}
