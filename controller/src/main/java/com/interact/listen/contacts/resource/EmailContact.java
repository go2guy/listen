package com.interact.listen.contacts.resource;

import com.interact.listen.resource.*;

import java.io.Serializable;
import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;

public class EmailContact extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(EmailContact.class);

    public enum Type
    {
        WORK,
    }
    
    private Long id;
    private String name;
    private String emailAddress;
    private Type type;
    
    public Long getId()
    {
        return id;
    }
    
    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getSubscriberId()
    {
        return id;
    }
    
    public void setSubscriberId(Long subscriberId)
    {
        this.id = subscriberId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getEmailAddress()
    {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
    
    public Type getType()
    {
        return type;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    
    @Override
    public boolean validate()
    {
        if(id == null || id.longValue() <= 0)
        {
            addToErrors("subscriber id not valid");
        }
        if(name == null || name.length() == 0)
        {
            addToErrors("name not provided");
        }
        if(emailAddress == null || emailAddress.length() == 0)
        {
            addToErrors("email address not provided");
        }
        if(type == null)
        {
            addToErrors("type not provided");
        }
        return !hasErrors();
    }

    @Override
    public Resource copy(boolean withIdAndVersion)
    {
        EmailContact ec = new EmailContact();

        if(withIdAndVersion)
        {
            ec.setId(getId());
        }
        ec.setName(getName());
        ec.setEmailAddress(getEmailAddress());
        ec.setType(getType());
        
        return ec;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[EmailContact ").append(id).append(" '").append(name);
        sb.append("' - '").append(emailAddress).append("' ").append(type).append(']');
        return sb.toString();
    }
    
    private static final Set<String> QUERY_FIELDS = new TreeSet<String>();
    static
    {
        QUERY_FIELDS.add("subscriberId");
        QUERY_FIELDS.add("name");
        QUERY_FIELDS.add("emailAddress");
        QUERY_FIELDS.add("type");
    }

    public static ResourceList queryForEmails(Session session, int first, int max)
    {
        final Criteria sCriteria = createCriteriaForAll(session, first, max, false);

        @SuppressWarnings("unchecked")
        List<Resource> contacts = (List<Resource>)sCriteria.list();
        
        LOG.debug("found " + contacts.size() + " records first=" + first + " max=" + max);
        
        final ResourceList rList = new ResourceList();
        
        rList.setFields(QUERY_FIELDS);
        rList.setList(contacts);
        rList.setFirst(first);
        rList.setMax(max);

        if(!contacts.isEmpty())
        {
            final Criteria cCriteria = createCriteriaForAll(session, first, max, true);
            rList.setTotal((Long)cCriteria.uniqueResult());
        }
        else
        {
            rList.setTotal(Long.valueOf(0));
        }

        return rList;
    }

    public static Resource queryById(Session session, Long id)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);

        addRestrictions(criteria, id);
        setProjection(criteria);

        return (Resource)criteria.uniqueResult();
    }
    
    private static Criteria createCriteriaForAll(Session session, int first, int max, boolean forCount)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);

        addRestrictions(criteria, null);
        
        if(forCount)
        {
            criteria.setFirstResult(0);
            criteria.setProjection(Projections.rowCount());
        }
        else
        {
            setProjection(criteria);
            criteria.setFirstResult(first);
            criteria.setMaxResults(max);
        }

        return criteria;
    }
    
    private static void addRestrictions(Criteria criteria, Long id)
    {
        Conjunction r = Restrictions.conjunction();
        if(id != null)
        {
            r.add(Restrictions.eq("id", id));
        }
        r.add(Restrictions.isNotNull("realName"));
        r.add(Restrictions.ne("workEmailAddress", ""));
        r.add(Restrictions.ne("realName", ""));
        
        criteria.add(r);
    }
    
    private static final ResultTransformer TRANSFORMER = new EmailContactTransformer();

    private static void setProjection(Criteria criteria)
    {
        criteria.setProjection(Projections.projectionList()
                               .add(Projections.property("id"), "subscriberId")
                               .add(Projections.property("realName"), "name")
                               .add(Projections.property("workEmailAddress"), "email"));
        criteria.setResultTransformer(TRANSFORMER);
    }
    
    private static class EmailContactTransformer extends BasicTransformerAdapter
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases)
        {
            EmailContact c = new EmailContact();
            for(int i = 0; i < tuple.length; ++i)
            {
                if(aliases[i] == null)
                {
                    LOG.warn("null alias at index " + i);
                    continue;
                }
                if(aliases[i].equals("subscriberId"))
                {
                    c.setSubscriberId((Long)tuple[i]);
                }
                else if(aliases[i].equals("name"))
                {
                    c.setName((String)tuple[i]);
                }
                else if(aliases[i].equals("email"))
                {
                    c.setEmailAddress((String)tuple[i]);
                }
            }
            c.setType(Type.WORK);
            if(LOG.isDebugEnabled())
            {
                LOG.debug("transformed " + c);
            }
            return c;
        }
    }

}
