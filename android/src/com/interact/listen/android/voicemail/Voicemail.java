package com.interact.listen.android.voicemail;

import java.text.SimpleDateFormat;

import android.util.Log;

public class Voicemail
{
    private Boolean isNew = Boolean.TRUE;
    private String leftBy;
    private String description;
    private String dateCreated;
    private String duration;
    private String transcription;
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm yyyy-MM-dd");
    
    public Voicemail(String isNew, String leftBy, String description, String dateCreated, String duration, String transcription)
    {
    	this.isNew = Boolean.valueOf(isNew);
    	this.leftBy = leftBy;
    	this.description = description;
    	
    	try
    	{
    		this.dateCreated = sdf2.format(sdf.parse(dateCreated));
    	}
    	catch(Exception e)
    	{
    		Log.e("Listen", "Error parsing date for voicemail " + e);
    		this.dateCreated = dateCreated;
    	}
    	
    	this.duration = duration;
    	this.transcription = transcription;
    }

    public Boolean getIsNew()
    {
        return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    public String getLeftBy()
    {
        return leftBy;
    }

    public void setLeftBy(String leftBy)
    {
        this.leftBy = leftBy;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public String getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated)
    {
        this.dateCreated = dateCreated;
    }
    
    public String getDuration()
    {
        return duration;
    }

    public void setDuration(String duration)
    {
        this.duration = duration;
    }

    public String getTranscription()
    {
        return transcription;
    }

    public void setTranscription(String transcription)
    {
        this.transcription = transcription;
    }

    public boolean hasTranscription()
    {
        return transcription != null && transcription.trim().length() > 0;
    }
}
