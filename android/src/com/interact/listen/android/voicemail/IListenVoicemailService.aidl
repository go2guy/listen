package com.interact.listen.android.voicemail;

import com.interact.listen.android.voicemail.Voicemail;

interface IListenVoicemailService {
	List<Voicemail> getVoicemails();
	boolean updateNotificationStatus(in long[] ids);
}