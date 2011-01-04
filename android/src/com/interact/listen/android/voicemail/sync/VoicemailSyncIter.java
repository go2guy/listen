package com.interact.listen.android.voicemail.sync;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

final class VoicemailSyncIter extends ListenSyncIter<Voicemail>
{
    private List<Voicemail> localVoicemails,  serverVoicemails;

    // assumes both lists are sorted by voicemail id
    VoicemailSyncIter(List<Voicemail> localVoicemails, List<Voicemail> serverVoicemails)
    {
        super(localVoicemails.iterator(), serverVoicemails.iterator());
        
        this.localVoicemails = localVoicemails;
        this.serverVoicemails = serverVoicemails;
    }
    
    public void reset()
    {
        reset(localVoicemails.iterator(), serverVoicemails.iterator());
    }

}
