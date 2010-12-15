package com.interact.listen.resource;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
public class ConferenceRecording extends Audio
{
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "CONFERENCE_ID")
    @ManyToOne
    private Conference conference;

    public Conference getConference()
    {
        return conference;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    @Override
    public ConferenceRecording copy(boolean withIdAndVersion)
    {
        ConferenceRecording copy = new ConferenceRecording();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDateCreated(getDateCreated() == null ? null : new Date(getDateCreated().getTime()));
        copy.setUri(getUri());
        copy.setDescription(getDescription());
        copy.setDuration(getDuration());
        copy.setFileSize(getFileSize());
        copy.setConference(conference);
        copy.setTranscription(getTranscription());
        return copy;
    }

    public static ConferenceRecording queryById(Session session, Long id)
    {
        return (ConferenceRecording)session.get(ConferenceRecording.class, id);
    }

    public static List<ConferenceRecording> queryByConferencePaged(Session session, Conference conference, int first,
                                                                   int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(ConferenceRecording.class);
        subquery.createAlias("conference", "conference_alias");
        subquery.add(Restrictions.eq("conference_alias.id", conference.getId()));
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(ConferenceRecording.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.desc("dateCreated"));

        criteria.setFetchMode("conference", FetchMode.SELECT);
        return (List<ConferenceRecording>)criteria.list();
    }

    public static Long countByConference(Session session, Conference conference)
    {
        Criteria criteria = session.createCriteria(ConferenceRecording.class);
        criteria.setProjection(Projections.rowCount());
        criteria.createAlias("conference", "conference_alias");
        criteria.add(Restrictions.eq("conference_alias.id", conference.getId()));
        return (Long)criteria.list().get(0);
    }
}
