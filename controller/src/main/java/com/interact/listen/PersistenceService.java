package com.interact.listen;

import com.interact.listen.history.Channel;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;

import org.hibernate.Session;

public interface PersistenceService
{
    public Resource get(Class<? extends Resource> resourceClass, Long id);
    public Long save(Resource resource);
    public void update(Resource updatedResource, Resource originalResource);
    public void delete(Resource resource);
    
    public Channel getChannel();
    public Subscriber getCurrentSubscriber();
    public String getCurrentDeviceId();
    public Session getSession();
    
    public void setCurrentDeviceId(String deviceId);
}
