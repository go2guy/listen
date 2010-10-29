package com.interact.listen.android.voicemail.controller;

import com.interact.listen.android.voicemail.Voicemail;

import java.util.List;

public interface Controller
{
	// Subscriber interactions
	public Long getSubscriberIdFromUsername(String api, String username, String encodedUsername, String password) throws ControllerException, 
					ConnectionException, UserNotFoundException, AuthorizationException;

	// Voicemail interactions
	public List<Voicemail> retrieveVoicemails(String api, Long subscriberId, String username, String password) throws ControllerException,
			ConnectionException, AuthorizationException;
	public void markVoicemailsNotified(String api, long[] ids, String username, String password) throws ControllerException, ConnectionException,
			AuthorizationException;
	public void markVoicemailsRead(String api, Long[] ids, String username, String password) throws ControllerException, ConnectionException,
			AuthorizationException;
	public void deleteVoicemails(String api, Long[] ids, String username, String password) throws ConnectionException, ControllerException, 
			AuthorizationException;
	public String downloadVoicemailToTempFile(String api, Long id, String username, String password) throws ConnectionException, ControllerException,
			AuthorizationException;
}
