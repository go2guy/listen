package com.interact.listen;

import com.interact.listen.resource.Resource;

import org.hibernate.Session;

public class PersistenceService
{
    private Session session;

    public PersistenceService(Session session)
    {
        this.session = session;
    }

    public Resource get(Class<? extends Resource> resourceClass, Long id)
    {
        return (Resource)session.get(resourceClass, id);
    }

    public Long save(Resource resource)
    {
        return (Long)session.save(resource);
    }

    public void update(Resource resource)
    {
        session.update(resource);
    }

    public void delete(Resource resource)
    {
        session.delete(resource);
    }
}
