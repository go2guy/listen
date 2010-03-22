package com.interact.listen.resource;

import com.interact.listen.HibernateUtil;

import javax.persistence.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

@Entity
public class Voicemail implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    @ManyToOne
    private Subscriber subscriber;

    private String fileLocation;

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

    @Override
    public String toXml(boolean deep)
    {
        StringBuilder xml = new StringBuilder();
        if(deep)
        {
            xml.append("<voicemail href=\"/voicemails/").append(id).append("\">");
            xml.append("<id>").append(id).append("</id>");
            xml.append(subscriber.toXml(false));
            xml.append("<fileLocation>").append(fileLocation).append("</fileLocation>");
            xml.append("</voicemail>");
        }
        else
        {
            xml.append("<voicemail href=\"/voicemails/").append(id).append("\"/>");
        }
        return xml.toString();
    }

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
        if(loadId)
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

        this.fileLocation = XmlUtil.getTagContents("fileLocation", xml);
    }
}
