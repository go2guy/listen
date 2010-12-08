package com.interact.listen.android.voicemail.sync;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.Iterator;
import java.util.List;

final class SyncIter
{
    private List<Voicemail> localVoicemails,  serverVoicemails;
    
    private Iterator<Voicemail> lIter, sIter;
    private Voicemail lVoicemail, sVoicemail;

    // assumes both lists are sorted by voicemail id
    SyncIter(List<Voicemail> localVoicemails, List<Voicemail> serverVoicemails)
    {
        this.localVoicemails = localVoicemails;
        this.serverVoicemails = serverVoicemails;

        reset();
    }
    
    public void reset()
    {
        lIter = localVoicemails.iterator();
        sIter = serverVoicemails.iterator();
        lVoicemail = null;
        sVoicemail = null;
    }
    
    public boolean next()
    {
        if(isMissingFromLocal())
        {
            sVoicemail = nextIter(sIter);
        }
        else if(isMissingFromServer())
        {
            lVoicemail = nextIter(lIter);
        }
        else // isMatched() or both null
        {
            sVoicemail = nextIter(sIter);
            lVoicemail = nextIter(lIter);
        }
        
        return lVoicemail != null || sVoicemail != null;
    }
    
    public boolean nextMatch()
    {
        while(next() && lVoicemail != null && sVoicemail != null)
        {
            if(isMatched())
            {
                return true;
            }
        }
        return false;
    }

    public Voicemail getLocal()
    {
        return lVoicemail;
    }
    public Voicemail getServer()
    {
        return sVoicemail;
    }
    
    public boolean isMissingFromLocal()
    {
        return sVoicemail != null && (lVoicemail == null || lVoicemail.getVoicemailId() > sVoicemail.getVoicemailId());
    }
    
    public boolean isMissingFromServer()
    {
        return lVoicemail != null && (sVoicemail == null || sVoicemail.getVoicemailId() > lVoicemail.getVoicemailId());
    }

    public boolean isMatched()
    {   // isMatched() == (!isMissingFromLocal() && !isMissingFromServer())
        return lVoicemail == null || sVoicemail == null ? false : lVoicemail.getVoicemailId() == sVoicemail.getVoicemailId();
    }
    
    private Voicemail nextIter(Iterator<Voicemail> iter)
    {
        return iter.hasNext() ? iter.next() : null;
    }

}
