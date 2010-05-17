package com.interact.listen;

import com.interact.listen.resource.Resource;

import org.hibernate.Session;

/**
 * Wrapper service for performing operations that persist {@link Resource}s. This allows for invoking {@code Resource}
 * callback methods (e.g. {@link Resource#afterSave(Session)} on persistence operations.
 */
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
        resource.beforeSave(session);
        Long id = (Long)session.save(resource);
        resource.afterSave(session);

        return id;
    }

    public void update(Resource updatedResource, Resource originalResource)
    {
        updatedResource.beforeUpdate(session, originalResource);
        session.update(updatedResource);
        updatedResource.afterUpdate(session, originalResource);
    }

    public void delete(Resource resource)
    {
        resource.beforeDelete(session);
        session.delete(resource);
        resource.afterDelete(session);
    }
}
