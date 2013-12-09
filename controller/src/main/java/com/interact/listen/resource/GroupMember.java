package com.interact.listen.resource;

import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Note: this class has a natural ordering that is inconsistent with equals
 */
@Entity
@Table(name = "group_member")
public class GroupMember extends Resource implements Comparable<GroupMember>
{
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = Boolean.FALSE;

    @Column(name = "GROUP_NAME")
    private String groupName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public Boolean getIsAdministrator()
    {
        return isAdministrator;
    }

    public void setIsAdministrator(Boolean isAdministrator)
    {
        this.isAdministrator = isAdministrator;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    @Override
    public Resource copy(boolean withIdAndVersion)
    {
        GroupMember copy = new GroupMember();
        copy.setGroupName(groupName);
        copy.setIsAdministrator(isAdministrator);
        copy.setSubscriber(subscriber);

        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }
        return copy;
    }

    @Override
    public boolean validate()
    {
        if(groupName == null || groupName.trim().equals(""))
        {
            addToErrors("Group name cannot be null");
        }

        if(subscriber == null)
        {
            addToErrors("Subscriber cannot be null");
        }

        return !hasErrors();
    }

    public static Map<String, Set<GroupMember>> queryAllInGroups(Session session)
    {
        Criteria criteria = session.createCriteria(GroupMember.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<GroupMember> results = criteria.list();

        Map<String, Set<GroupMember>> groups = new HashMap<String, Set<GroupMember>>();
        for(GroupMember member : results)
        {
            if(!groups.containsKey(member.getGroupName()))
            {
                groups.put(member.getGroupName(), new TreeSet<GroupMember>());
            }
            groups.get(member.getGroupName()).add(member);
        }
        return groups;
    }

    public static void deleteAll(Session session)
    {
        String hql = "delete from GroupMember";
        org.hibernate.Query query = session.createQuery(hql);
        query.executeUpdate();
    }

    @Override
    public int compareTo(GroupMember other)
    {
        return subscriber.friendlyName().compareTo(other.getSubscriber().friendlyName());
    }
}
