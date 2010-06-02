package com.interact.listen.resource;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "CONFERENCE_RECORDING")
public class ConferenceRecording extends Audio
{
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "CONFERENCE_ID")
    @ManyToOne
    private Conference conference;

    public Conference getConference()
    {
        return conference;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    @Override
    public ConferenceRecording copy(boolean withIdAndVersion)
    {
        ConferenceRecording copy = new ConferenceRecording();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDateCreated(getDateCreated() == null ? null : new Date(getDateCreated().getTime()));
        copy.setUri(getUri());
        copy.setDescription(getDescription());
        copy.setDuration(getDuration());
        copy.setFileSize(getFileSize());
        copy.setConference(conference);
        return copy;
    }
}
