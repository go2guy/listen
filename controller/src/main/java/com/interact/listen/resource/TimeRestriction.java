package com.interact.listen.resource;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

<<<<<<< .mine
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.annotations.Type;
import org.hibernate.criterion.Restrictions;
=======
import org.hibernate.annotations.Type;
>>>>>>> .r1612
import org.joda.time.LocalTime;

@Entity
@Table(name = "TIME_RESTRICTION")
public class TimeRestriction extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "START_ENTRY")
    private String startEntry;

    @Column(name = "END_ENTRY")
    private String endEntry;
    
    @Column(name = "START_TIME", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsString")
    private LocalTime startTime;
    
    @Column(name = "END_TIME", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsString")
    private LocalTime endTime;
    
    @Column(name = "ACTION")
    @Enumerated(EnumType.STRING)
    private Action action;
    
    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "MONDAY")
    private Boolean monday = false;
    
    @Column(name = "TUESDAY")
    private Boolean tuesday = false;
    
    @Column(name = "WEDNESDAY")
    private Boolean wednesday = false;
    
    @Column(name = "THURSDAY")
    private Boolean thursday = false;
    
    @Column(name = "FRIDAY")
    private Boolean friday = false;
    
    @Column(name = "SATURDAY")
    private Boolean saturday = false;
    
    @Column(name = "SUNDAY")
    private Boolean sunday = false;

    public enum Action
    {
        NEW_VOICEMAIL_EMAIL, NEW_VOICEMAIL_SMS;
    }

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

    public String getStartEntry()
    {
        return startEntry;
    }

    public void setStartEntry(String startEntry)
    {
        this.startEntry = startEntry;
    }

    public String getEndEntry()
    {
        return endEntry;
    }

    public void setEndEntry(String endEntry)
    {
        this.endEntry = endEntry;
    }

    public LocalTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime(LocalTime startTime)
    {
        this.startTime = startTime;
    }

    public LocalTime getEndTime()
    {
        return endTime;
    }

    public void setEndTime(LocalTime endTime)
    {
        this.endTime = endTime;
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }
    
    public Boolean getMonday()
    {
        return monday;
    }

    public void setMonday(Boolean monday)
    {
        this.monday = monday;
    }

    public Boolean getTuesday()
    {
        return tuesday;
    }

    public void setTuesday(Boolean tuesday)
    {
        this.tuesday = tuesday;
    }

    public Boolean getWednesday()
    {
        return wednesday;
    }

    public void setWednesday(Boolean wednesday)
    {
        this.wednesday = wednesday;
    }

    public Boolean getThursday()
    {
        return thursday;
    }

    public void setThursday(Boolean thursday)
    {
        this.thursday = thursday;
    }

    public Boolean getFriday()
    {
        return friday;
    }

    public void setFriday(Boolean friday)
    {
        this.friday = friday;
    }

    public Boolean getSaturday()
    {
        return saturday;
    }

    public void setSaturday(Boolean saturday)
    {
        this.saturday = saturday;
    }

    public Boolean getSunday()
    {
        return sunday;
    }

    public void setSunday(Boolean sunday)
    {
        this.sunday = sunday;
    }

    @Override
    public boolean validate()
    {
        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        if(startTime == null)
        {
            addToErrors("startTime cannot be null");
        }
        
        if(endTime == null)
        {
            addToErrors("endTime cannot be null");
        }

        return !hasErrors();
    }
    
    @Override
    public TimeRestriction copy(boolean withIdAndVersion)
    {
        TimeRestriction copy = new TimeRestriction();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setStartEntry(getStartEntry());
        copy.setEndEntry(getEndEntry());
        copy.setStartTime(getStartTime());
        copy.setEndTime(getEndTime());
        copy.setAction(getAction());
        copy.setSubscriber(subscriber);
        copy.setMonday(monday);
        copy.setTuesday(tuesday);
        copy.setWednesday(wednesday);
        copy.setThursday(thursday);
        copy.setFriday(friday);
        copy.setSaturday(saturday);
        copy.setSunday(sunday);
        return copy;
    }
    
    public static List<TimeRestriction> queryBySubscriberAndAction(Session session, Subscriber subscriber, Action action)
    {
        Criteria criteria = session.createCriteria(TimeRestriction.class);
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        criteria.add(Restrictions.eq("action", action));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (List<TimeRestriction>)criteria.list();
    }
    
    public static void deleteBySubscriberAndAction(Session session, Subscriber subscriber, Action action)
    {
        org.hibernate.Query query = session.createQuery("delete from TimeRestriction t where t.subscriber.id=:subscriberId and t.action=:action");
        query.setLong("subscriberId", subscriber.getId());
        query.setString("action", action.toString());
        query.executeUpdate();
    }
    
    private enum Meridiem {
        AM,
        PM,
        NONE;
    }

    public static LocalTime parseTime(String entry)
    {
        if(!entry.matches("^[0-9]{1,2}:?[0-9]{2}(AM|PM)?$"))
        {
            throw new IllegalArgumentException("'" + entry + "' is not a valid time");
        }

        String normalized = entry.replaceAll(":", "");
        Meridiem meridiem = Meridiem.NONE;
        if(entry.endsWith("M"))
        {
            String m = normalized.substring(entry.length() - 3);
            meridiem = m.equals("AM") ? Meridiem.AM : Meridiem.PM;
            normalized = normalized.substring(0, normalized.length() - 2);
        }

        int hours = Integer.parseInt(normalized.substring(0, normalized.length() == 3 ? 1 : 2));
        int minutes = Integer.parseInt(normalized.substring(normalized.length() == 3 ? 1 : 2));

        if(hours < 0 || hours > 23 || minutes < 0 || minutes > 59)
        {
            throw new IllegalArgumentException("'" + entry + "' is not a valid time");
        }

        if(meridiem == Meridiem.PM && hours >= 1 && hours <= 11)
        {
            hours += 12;
        }

        return new LocalTime(hours, minutes);
    }
}
