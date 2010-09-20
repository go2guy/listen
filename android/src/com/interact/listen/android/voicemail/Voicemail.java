package com.interact.listen.android.voicemail;

import java.text.SimpleDateFormat;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Voicemail implements Parcelable
{
    private long id;
    private Boolean isNew = Boolean.TRUE;
    private String leftBy;
    private String description;
    private String dateCreated;
    private String duration;
    private String transcription = "Transcription not available";
    private Boolean hasNotified = Boolean.FALSE;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm yyyy-MM-dd");

    public static final Parcelable.Creator<Voicemail> CREATOR = new Parcelable.Creator<Voicemail>()
    {
        public Voicemail createFromParcel(Parcel in)
        {
            return new Voicemail(in);
        }

        public Voicemail[] newArray(int size)
        {
            return new Voicemail[size];
        }
    };

    private Voicemail(Parcel in)
    {
        readFromParcel(in);
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags)
    {
        out.writeLong(this.id);
        out.writeString(this.isNew ? "true" : "false");
        out.writeString(this.leftBy);
        out.writeString(this.description);
        out.writeString(this.dateCreated);
        out.writeString(this.duration);
        out.writeString(this.transcription);
        out.writeString(this.hasNotified ? "true" : "false");
    }

    public void readFromParcel(Parcel in)
    {
        this.id = in.readLong();
        this.isNew = Boolean.valueOf(in.readString());
        this.leftBy = in.readString();
        this.description = in.readString();
        this.dateCreated = in.readString();
        this.duration = in.readString();
        this.transcription = in.readString();
        this.hasNotified = Boolean.valueOf(in.readString());
    }

    public Voicemail(String id, String isNew, String leftBy, String description, String dateCreated, String duration,
                     String transcription, String hasNotified)
    {
        this.id = Long.valueOf(id);
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

        if(transcription != null && !transcription.equals("null") && !transcription.equals(""))
        {
            this.transcription = transcription;
        }
        
        this.hasNotified = Boolean.valueOf(hasNotified);
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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
    
    public Boolean getHasNotified()
    {
        return hasNotified;
    }

    public void setHasNotified(Boolean hasNotified)
    {
        this.hasNotified = hasNotified;
    }
}
