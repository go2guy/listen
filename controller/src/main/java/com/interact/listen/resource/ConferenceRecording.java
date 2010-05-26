package com.interact.listen.resource;

import java.util.Date;

import javax.persistence.*;

@Entity
public class ConferenceRecording extends Audio
{
    private static final long serialVersionUID = 1L;

    @JoinTable(name = "CONFERENCE_CONFERENCE_RECORDING",
               joinColumns = @JoinColumn(name = "CONFERENCE_RECORDING_ID"),
               inverseJoinColumns = @JoinColumn(name = "CONFERENCE_ID"))
    @ManyToOne(optional = true)
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
        copy.setFileSize(getFileSize());
        copy.setConference(conference);
        return copy;
    }
}
