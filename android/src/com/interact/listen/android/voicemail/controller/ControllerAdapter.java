package com.interact.listen.android.voicemail.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.Voicemail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class ControllerAdapter
{
    private static final String TAG = ControllerAdapter.class.getName();
    private static final int LISTEN_COMMUNICATION_ERROR_NOTIFICATION = 46;
    private Controller controller;

    public ControllerAdapter(Controller controller)
    {
        this.controller = controller;
    }

    public Long getSubscriberIdFromUsername(Context context)
    {
    	// special case of property verification.  We will do this query even if the sub id is -1 since 
    	// that is the property we are trying to update
    	if (!ApplicationSettings.getApi(context).startsWith("http://:") &&
    		!ApplicationSettings.getUsername(context).equals(""))
    	{
    		try
            {
                return controller.getSubscriberIdFromUsername(ApplicationSettings.getApi(context),
                                                              ApplicationSettings.getUsername(context));
            }
            catch(ControllerException e)
            {
                notifyConnectionError(context, e);
                Log.e(TAG, "Controller communication error", e);
            }
            catch(ConnectionException e)
            {
                notifyConnectionError(context, e.getApi());
                Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
            }
            catch(UserNotFoundException e)
            {
                notifyConnectionError(context, "User not found with id '" + e.getUsername() + "'");
                Log.w(TAG, "User not found on controller with id [" + e.getUsername() + "]", e);
            }
            catch(AuthorizationException e)
        	{
        		notifyConnectionError(context, "User unauthorized at " + e.getApi());
                Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
        	}
    	}

        return -1L;
    }

    public List<Voicemail> retrieveVoicemails(Context context)
    {
    	String base64Username = getBase64EncodedString(ApplicationSettings.getUsername(context));
    	String base64Password = getBase64EncodedString(ApplicationSettings.getPassword(context));
    	
    	if(verifyConnectionProperties(context))
    	{
    		try
            {
				List<Voicemail> voicemails = controller.retrieveVoicemails(ApplicationSettings.getApi(context),
																		   ApplicationSettings.getSubscriberId(context),
																		   base64Username, base64Password);
            	
            	//Clear the notification bar in case there had been an error notification there
                clearNotificationBar(context);
                
                return voicemails;
            }
            catch(ControllerException e)
            {
                notifyConnectionError(context, e);
                Log.e(TAG, "Controller communication error", e);
            }
            catch(ConnectionException e)
            {
                notifyConnectionError(context, e.getApi());
                Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
            }
            catch(AuthorizationException e)
        	{
        		notifyConnectionError(context, "User unauthorized at " + e.getApi());
                Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
        	}
    	}

        return new ArrayList<Voicemail>();
    }

    public void markVoicemailsNotified(Context context, long[] ids)
    {
    	String base64Username = getBase64EncodedString(ApplicationSettings.getUsername(context));
    	String base64Password = getBase64EncodedString(ApplicationSettings.getPassword(context));
    	
    	if(verifyConnectionProperties(context))
    	{
    		try
            {
                controller.markVoicemailsNotified(ApplicationSettings.getApi(context), ids, base64Username, base64Password);
            }
            catch(ControllerException e)
            {
                notifyConnectionError(context, e);
                Log.e(TAG, "Controller communication error", e);
            }
            catch(ConnectionException e)
            {
                notifyConnectionError(context, e.getApi());
                Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
            }
            catch(AuthorizationException e)
        	{
        		notifyConnectionError(context, "User unauthorized at " + e.getApi());
                Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
        	}
    	}
    }

    public void markVoicemailsRead(Context context, Long[] ids)
    {
    	String base64Username = getBase64EncodedString(ApplicationSettings.getUsername(context));
    	String base64Password = getBase64EncodedString(ApplicationSettings.getPassword(context));
    	
    	if(verifyConnectionProperties(context))
    	{
    		try
            {
                controller.markVoicemailsRead(ApplicationSettings.getApi(context), ids, base64Username, base64Password);
            }
            catch(ControllerException e)
            {
                notifyConnectionError(context, e);
                Log.e(TAG, "Controller communication error", e);
            }
            catch(ConnectionException e)
            {
                notifyConnectionError(context, e.getApi());
                Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
            }
            catch(AuthorizationException e)
        	{
        		notifyConnectionError(context, "User unauthorized at " + e.getApi());
                Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
        	}
    	}
    }

    public void deleteVoicemails(Context context, Long[] ids)
    {
    	String base64Username = getBase64EncodedString(ApplicationSettings.getUsername(context));
    	String base64Password = getBase64EncodedString(ApplicationSettings.getPassword(context));
    	
    	if(verifyConnectionProperties(context))
    	{
    		try
            {
                controller.deleteVoicemails(ApplicationSettings.getApi(context), ids, base64Username, base64Password);
            }
            catch(ConnectionException e)
            {
                notifyConnectionError(context, e.getApi());
                Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
            }
            catch(AuthorizationException e)
        	{
        		notifyConnectionError(context, "User unauthorized at " + e.getApi());
                Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
        	}
    	}
    }
    
    public String downloadVoicemailToTempFile(Context context, Long id)
    {
    	String base64Username = getBase64EncodedString(ApplicationSettings.getUsername(context));
    	String base64Password = getBase64EncodedString(ApplicationSettings.getPassword(context));
    	
    	try
    	{
    		return controller.downloadVoicemailToTempFile(ApplicationSettings.getApi(context), id, base64Username, base64Password);
    	}
    	catch(ConnectionException e)
    	{
    		notifyConnectionError(context, e.getApi());
            Log.e(TAG, "Controller connection error for api [" + e.getApi() + "]", e);
    	}
    	catch(AuthorizationException e)
    	{
    		notifyConnectionError(context, "User unauthorized at " + e.getApi());
            Log.e(TAG, "Controller authorization error for api [" + e.getApi() + "]", e);
    	}
    	
    	return "";
    }

    private void notifyConnectionError(Context context, Throwable t)
    {
        notifyConnectionError(context, t.getMessage());
    }
    
    private boolean verifyConnectionProperties(Context context)
    {
    	//Check if a host has been configured along with a username and valid subscriber id
		if (ApplicationSettings.getApi(context).startsWith("http://:") ||
			ApplicationSettings.getUsername(context).equals("") ||
			ApplicationSettings.getSubscriberId(context) == -1)
    	{
			return false;
    	}
		
    	return true;
    }

    private void notifyConnectionError(Context context, String message)
    {
        Log.v(TAG, "notifyConnectionError()");
        String title = "Listen connection error";

        int icon = R.drawable.notification_bar_icon_error;
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, title, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        Intent intent = new Intent(context, ApplicationSettings.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        notification.setLatestEventInfo(context, title, message, pIntent);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(LISTEN_COMMUNICATION_ERROR_NOTIFICATION, notification);
    }
    
    private void clearNotificationBar(Context context)
    {
        Log.v(TAG, "clearNotificationBar()");
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(LISTEN_COMMUNICATION_ERROR_NOTIFICATION);
    }
    
    private String getBase64EncodedString(String stringToEncode)
    {
    	return new String(Base64.encodeBase64(stringToEncode.getBytes()));
    }
}
