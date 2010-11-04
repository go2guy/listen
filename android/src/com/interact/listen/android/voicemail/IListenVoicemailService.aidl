package com.interact.listen.android.voicemail;

import com.interact.listen.android.voicemail.Voicemail;

interface IListenVoicemailService {
	void deleteVoicemail(in long id);
	List<Voicemail> getVoicemails();
	void markVoicemailOld(in long id);
	void markVoicemailsNotified(in long[] ids);
	void startPolling();
	void stopPolling();
}