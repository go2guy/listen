package com.interact.listen.resource;

import com.interact.listen.xml.XmlUtil;

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
    private Conference conference;

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

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    public Conference getConference()
    {
        return conference;
    }


    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public void loadFromXml(String xml, boolean loadId)
    {
        // FIXME super-gross xml parsing until I get a decent xml binding framework in place
        if(loadId && xml.contains("<id>"))
        {
            this.id = Long.parseLong(XmlUtil.getTagContents("id", xml));
        }

        if(xml.contains("<conference"))
        {
            String href = XmlUtil.getAttributeValue("conference", "href", xml);
            Long id = Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            this.conference = (Conference)session.get(Conference.class, id);
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
