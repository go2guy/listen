package com.interact.listen.resource;

import com.interact.listen.HibernateUtil;
import com.interact.listen.xml.XmlUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

@Entity
public class Voicemail implements Resource
{
    public static final String DATE_CREATED_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    @ManyToOne
    private Subscriber subscriber;

    private String fileLocation;
    private Date dateCreated = new Date();
    private Boolean isNew = Boolean.TRUE;

    @Override
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

    public String getFileLocation()
    {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    public Date getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public Boolean getIsNew()
    {
        return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
        if(loadId && xml.contains("<id>"))
        {
            this.id = Long.parseLong(XmlUtil.getTagContents("id", xml));
        }

        if(xml.contains("<subscriber"))
        {
            String href = XmlUtil.getAttributeValue("subscriber", "href", xml);
            Long id = Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            this.subscriber = (Subscriber)session.get(Subscriber.class, id);
            transaction.commit();
        }

        if(xml.contains("<fileLocation>"))
        {
            this.fileLocation = XmlUtil.getTagContents("fileLocation", xml);
        }

        if(xml.contains("<dateCreated>"))
        {
            try
            {
                Date date = new SimpleDateFormat(DATE_CREATED_FORMAT).parse(XmlUtil.getTagContents("dateCreated", xml));
                this.dateCreated = date;
            }
            catch(ParseException e)
            {
                // TODO throw
                e.printStackTrace();
            }
        }

        if(xml.contains("<isNew>"))
        {
            this.isNew = Boolean.parseBoolean(XmlUtil.getTagContents("isNew", xml));
        }
    }
}
