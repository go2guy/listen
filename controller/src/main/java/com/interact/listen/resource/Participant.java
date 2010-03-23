package com.interact.listen.resource;

import javax.persistence.*;


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

    private Boolean admin;
    private Boolean holding;
	private Boolean muted;	 
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

	public Boolean getIsAdmin()
	{
		return admin;
	}

	public void setIsAdmin(Boolean admin)
	{
		this.admin = admin;
	}

	public Boolean getIsHolding()
	{
		return holding;
	}

	public void setIsHolding(Boolean holding)
	{
		this.holding = holding;
	}

	public Boolean getIsMuted()
	{
		return muted;
	}

	public void setIsMuted(Boolean muted)
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
}
