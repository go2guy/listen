package com.interact.listen;

import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.lang.reflect.Method;
import java.util.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

public final class ResourceListService
{
    private Class<? extends Resource> resourceClass;
    private Session session;
    private Marshaller marshaller;

    private Map<String, String> searchProperties = new HashMap<String, String>();
    private Set<String> returnFields = new HashSet<String>();
    private int first = 0;
    private int max = 100;
    private boolean uniqueResult = false;
    private boolean or = false;
    private String sortColumn = "id";
    private SortOrder sortOrder = SortOrder.ASCENDING;

    public enum SortOrder
    {
        ASCENDING, DESCENDING;
    }

    private ResourceListService(Builder builder)
    {
        this.resourceClass = builder.resourceClass;
        this.session = builder.session;
        this.marshaller = builder.marshaller;
        this.searchProperties = builder.searchProperties;
        this.returnFields = builder.returnFields;
        this.first = builder.first;
        this.max = builder.max;
        this.uniqueResult = builder.uniqueResult;
        this.or = builder.or;
        this.sortColumn = builder.sortColumn;
        this.sortOrder = builder.sortOrder;
    }

    public static class Builder
    {
        // required fields
        private Class<? extends Resource> resourceClass;
        private Session session;
        private Marshaller marshaller;

        private Map<String, String> searchProperties = new HashMap<String, String>();
        private Set<String> returnFields = new HashSet<String>();
        private int first = 0;
        private int max = 100;
        private boolean uniqueResult = false;
        private boolean or = false;
        private String sortColumn = "id";
        private SortOrder sortOrder = SortOrder.ASCENDING;

        public Builder(Class<? extends Resource> resourceClass, Session session, Marshaller marshaller)
        {
            this.resourceClass = resourceClass;
            this.session = session;
            this.marshaller = marshaller;
        }

        public Builder addSearchProperty(String key, String value)
        {
            this.searchProperties.put(key, value);
            return this;
        }

        public Builder addReturnField(String field)
        {
            this.returnFields.add(field);
            return this;
        }

        public Builder withFirst(int firstResult)
        {
            this.first = firstResult;
            return this;
        }

        public Builder withMax(int maxResults)
        {
            this.max = maxResults;
            return this;
        }

        public Builder uniqueResult(boolean unique)
        {
            this.uniqueResult = unique;
            return this;
        }

        public Builder or(boolean isOr)
        {
            this.or = isOr;
            return this;
        }

        public Builder sortBy(String column, SortOrder order)
        {
            this.sortColumn = column;
            this.sortOrder = order;
            return this;
        }

        public ResourceListService build()
        {
            return new ResourceListService(this);
        }
    }

    public String list() throws CriteriaCreationException
    {
        StringBuilder content = new StringBuilder();
        if(marshaller instanceof XmlMarshaller)
        {
            content.append(ApiServlet.XML_TAG);
        }

        Criteria criteria = createCriteria(false);
        List<Resource> results = (List<Resource>)criteria.list();

        if(uniqueResult)
        {
            if(results.size() == 0)
            {
                throw new UniqueResultNotFoundException();
            }

            content.append(marshaller.marshal(results.get(0)));
        }
        else
        {
            Long total = Long.valueOf(0);
            if(results.size() > 0)
            {
                criteria = createCriteria(true);
                total = (Long)criteria.list().get(0);
            }

            ResourceList list = new ResourceList();
            list.setList(results);
            list.setMax(max);
            list.setFirst(first);
            list.setSearchProperties(searchProperties);
            list.setFields(returnFields);
            list.setTotal(total);

            content.append(marshaller.marshal(list, resourceClass));
        }
        return content.toString();
    }

    /**
     * Creates a {@link Criteria} from the properties in this service.
     * 
     * @return criteria that can be used to list results
     */
    private Criteria createCriteria(boolean forCount) throws CriteriaCreationException
    {
        Criteria criteria = session.createCriteria(resourceClass);

        criteria.setMaxResults(max);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        if(!forCount)
        {
            criteria.setFirstResult(first);
            criteria.addOrder(sortOrder == SortOrder.ASCENDING ? Order.asc(sortColumn) : Order.desc(sortColumn));
        }
        else
        {
            criteria.setFirstResult(0);
            criteria.setProjection(Projections.rowCount());
        }

        Junction junction = or ? Restrictions.disjunction() : Restrictions.conjunction();

        for(Map.Entry<String, String> entry : searchProperties.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();

            String getMethod = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method method = Marshaller.findMethod(getMethod, resourceClass);
            if(method == null)
            {
                System.out.println("Resource [" + resourceClass + "] does not have getter for [" + key +
                                   "], continuing");
                continue;
            }

            try
            {
                if(Resource.class.isAssignableFrom(method.getReturnType()))
                {
                    Long id = Marshaller.getIdFromHref(value);
                    criteria.createAlias(key, key + "_alias");
                    junction.add(Restrictions.eq(key + "_alias.id", id));
                }
                else
                {
                    Class<? extends Converter> converterClass = Marshaller.getConverterClass(method.getReturnType());
                    Converter converter = converterClass.newInstance();
                    Object convertedValue = converter.unmarshal(value);
                    junction.add(Restrictions.eq(key, convertedValue));
                }
            }
            catch(IllegalAccessException e)
            {
                throw new AssertionError(e);
            }
            catch(java.lang.InstantiationException e)
            {
                throw new AssertionError(e);
            }
            catch(ConversionException e)
            {
                throw new CriteriaCreationException("Could not convert value [" + value + "] to type [" +
                                                    method.getReturnType() + "] for finding by [" + key + "]");
            }
        }

        criteria.add(junction);

        return criteria;
    }
}
