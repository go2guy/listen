package com.interact.listen;

import com.interact.listen.resource.ListenSpotSubscriber;
import com.interact.listen.resource.Participant;
import com.interact.listen.resource.Resource;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;

import java.io.IOException;
import java.util.List;

import org.hibernate.Criteria;
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

    public void update(Resource updatedResource, Resource originalResource) throws IOException,
        SpotCommunicationException
    {
        if(updatedResource instanceof Participant)
        {
            Participant updatedParticipant = (Participant)updatedResource;
            Participant originalParticipant = (Participant)originalResource;

            if(areMutedValuesDifferent(updatedParticipant, originalParticipant))
            {
                List<ListenSpotSubscriber> spotSubscribers = getSpotSubscribers();
                for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
                {
                    SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                    if(updatedParticipant.getIsAdminMuted().booleanValue())
                    {
                        spotSystem.muteParticipant(updatedParticipant);
                    }
                    else
                    {
                        spotSystem.unmuteParticipant(updatedParticipant);
                    }
                }
            }
        }

        session.update(updatedResource);
    }

    public void delete(Resource resource) throws IOException, SpotCommunicationException
    {
        if(resource instanceof Participant)
        {
            // FIXME what happens when the first one succeeds and the second one fails? do we "rollback" the first one?
            // there's no way we can do it with 100% reliability (because the "rollback" might fail, too)
            // - in all likelihood there will only be one Spot subscriber, but we should accommodate many
            List<ListenSpotSubscriber> spotSubscribers = getSpotSubscribers();
            for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
            {
                SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                spotSystem.dropParticipant((Participant)resource);
            }
        }

        session.delete(resource);
    }

    private boolean areMutedValuesDifferent(Participant p1, Participant p2)
    {
        return p1.getIsAdminMuted().booleanValue() != p2.getIsAdminMuted().booleanValue();
    }

    private List<ListenSpotSubscriber> getSpotSubscribers()
    {
        Criteria criteria = session.createCriteria(ListenSpotSubscriber.class);
        return criteria.list();
    }
}
