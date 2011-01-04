package com.interact.listen.contacts.resource;

import com.interact.listen.resource.*;
import com.interact.listen.resource.AccessNumber.NumberType;

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

public class NumberContact extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(NumberContact.class);
    
    private Long id;
    private Long subscriberId;
    private String name;
    private String number;
    private NumberType type;
    
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
        return subscriberId;
    }
    
    public void setSubscriberId(Long subscriberId)
    {
        this.subscriberId = subscriberId;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getNumber()
    {
        return number;
    }
    
    public void setNumber(String number)
    {
        this.number = number;
    }
    
    public NumberType getType()
    {
        return type;
    }
    
    public void setType(NumberType type)
    {
        this.type = type;
    }
    
    @Override
    public boolean validate()
    {
        if(id == null || id.longValue() <= 0)
        {
            addToErrors("access number id not valid");
        }
        if(subscriberId == null || subscriberId.longValue() <= 0)
        {
            addToErrors("subscriber id not valid");
        }
        if(name == null || name.length() == 0)
        {
            addToErrors("name not provided");
        }
        if(number == null || number.length() == 0)
        {
            addToErrors("number not provided");
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
        NumberContact nc = new NumberContact();

        if(withIdAndVersion)
        {
            nc.setId(getId());
        }
        nc.setSubscriberId(getSubscriberId());
        nc.setName(getName());
        nc.setNumber(getNumber());
        nc.setType(getType());
        
        return nc;
    }

    private static final Set<String> QUERY_FIELDS = new TreeSet<String>();
    static
    {
        QUERY_FIELDS.add("id");
        QUERY_FIELDS.add("subscriberId");
        QUERY_FIELDS.add("name");
        QUERY_FIELDS.add("number");
        QUERY_FIELDS.add("type");
    }
    
    public static ResourceList queryForNumbers(Session session, int first, int max)
    {
        final Criteria sCriteria = createCriteriaForAll(session, first, max, false);

        @SuppressWarnings("unchecked")
        List<Resource> contacts = (List<Resource>)sCriteria.list();

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
        Criteria criteria = initCriteria(session, id);

        setProjection(criteria);

        return (Resource)criteria.uniqueResult();
    }
    
    private static Criteria createCriteriaForAll(Session session, int first, int max, boolean forCount)
    {
        Criteria criteria = initCriteria(session, null);
        
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

    private static Criteria initCriteria(Session session, Long id)
    {
        Criteria criteria = session.createCriteria(AccessNumber.class);

        criteria.createAlias("subscriber", "subscriber_alias");

        Conjunction r = Restrictions.conjunction();
        if(id != null)
        {
            r.add(Restrictions.eq("id", id));
        }
        r.add(Restrictions.eq("publicNumber", true));
        r.add(Restrictions.ne("number", ""));
        r.add(Restrictions.isNotNull("subscriber_alias.realName"));
        r.add(Restrictions.ne("subscriber_alias.realName", ""));
        
        criteria.add(r);
        
        return criteria;
    }
    
    private static final ResultTransformer TRANSFORMER = new NumberContactTrasformer();

    private static void setProjection(Criteria criteria)
    {
        criteria.setProjection(Projections.projectionList()
                               .add(Projections.property("id"), "accessId")
                               .add(Projections.property("subscriber_alias.id"), "subscriberId")
                               .add(Projections.property("subscriber_alias.realName"), "name")
                               .add(Projections.property("number"), "phoneNumber")
                               .add(Projections.property("numberType"), "type"));
        criteria.setResultTransformer(TRANSFORMER);
    }
    
    private static class NumberContactTrasformer extends BasicTransformerAdapter
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases)
        {
            NumberContact c = new NumberContact();
            for(int i = 0; i < tuple.length; ++i)
            {
                if(aliases[i] == null)
                {
                    LOG.warn("null alias at index " + i);
                    continue;
                }
                if(aliases[i].equals("accessId"))
                {
                    c.setId((Long)tuple[i]);
                }
                if(aliases[i].equals("subscriberId"))
                {
                    c.setSubscriberId((Long)tuple[i]);
                }
                else if(aliases[i].equals("name"))
                {
                    c.setName((String)tuple[i]);
                }
                else if(aliases[i].equals("phoneNumber"))
                {
                    c.setNumber((String)tuple[i]);
                }
                else if(aliases[i].equals("type"))
                {
                    c.setType((NumberType)tuple[i]);
                }
            }
            if(LOG.isDebugEnabled())
            {
                LOG.debug("transformed " + c);
            }
            return c;
        }
    }

}
