package com.interact.listen.resource;

import javax.persistence.*;

import com.interact.listen.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;


@Entity
public class Participant implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    private String number;
   
    
    @ManyToOne
    private Conference confId;

    private boolean admin;
    private boolean holding;
	private boolean muted;	 
    private String audioResource;
    private String sessionID; 

	public String getAudioResource()
	{
		return audioResource;
	}
	
	public void setAudioResource(String audioResource)
	{
		this.audioResource = audioResource;
	}

	public String getSessionID()
	{
		return sessionID;
	}
	
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	private boolean	getAdmin()
	{
		return admin;
	}

	private void setAdmin(boolean admin)
	{
		this.admin = admin;
	}

	private boolean	getHolding()
	{
		return holding;
	}

	private void setHolding(boolean holding)
	{
		this.holding = holding;
	}

	private boolean	getMuted()
	{
		return muted;
	}

	private void setMuted(boolean muted)
	{
		this.muted = muted;
	}

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
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

    public void setConfId(Conference confId)
    {
        this.confId = confId;
    }

    public Conference getConfId()
    {
        return confId;
    }


    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public String toXml(boolean deep)
    {
        StringBuilder xml = new StringBuilder();

        if(deep)
        {
            xml.append("<participant href=\"/subscribers/").append(id).append("\">");
            xml.append("<id>").append(id).append("</id>");
            xml.append(confId.toXml(false));
            xml.append("<number>").append(number).append("</number>");
            xml.append("<muted>").append(muted).append("</muted>");
            xml.append("<holding>").append(holding).append("</holding>");
            xml.append("<admin>").append(admin).append("</admin>");
            xml.append("<audioResource>").append(audioResource).append("</audioResource>");
            xml.append("<sessionID>").append(sessionID).append("</sessionID>");
            xml.append("</participant>");
        }
        else
        {
            xml.append("<participant href=\"/subscribers/").append(id).append("\"/>");
        }
        return xml.toString();
    }

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
        // FIXME super-gross xml parsing until I get a decent xml binding framework in place
        if(loadId && xml.contains("<id>"))
        {
            this.id = Long.parseLong(XmlUtil.getTagContents("id", xml));
        }

        if(xml.contains("<confId"))
        {
            String href = XmlUtil.getAttributeValue("confId", "href", xml);
            Long id = Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            this.confId = (Conference)session.get(Conference.class, id);
            transaction.commit();
        }

        if(xml.contains("<sessionID"))
        {
            this.sessionID = XmlUtil.getTagContents("sessionID", xml);
        }

        if(xml.contains("<audioResource"))
        {
            this.audioResource = XmlUtil.getTagContents("audioResource", xml);
        }


        if(xml.contains("<number"))
        {
            this.number = XmlUtil.getTagContents("number", xml);
        }

        if(xml.contains("<admin>"))
        {
            this.admin = Boolean.parseBoolean(XmlUtil.getTagContents("admin", xml));
        }

        if(xml.contains("<holding>"))
        {
            this.holding = Boolean.parseBoolean(XmlUtil.getTagContents("holding", xml));
        }

        if(xml.contains("<muted>"))
        {
            this.muted = Boolean.parseBoolean(XmlUtil.getTagContents("muted", xml));
        }

    }
}
