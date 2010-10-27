package com.interact.listen.android.voicemail;

import com.interact.listen.android.voicemail.controller.ControllerAdapter;
import com.interact.listen.android.voicemail.controller.DefaultController;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class ApplicationSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    public static final String KEY_HOST_PREFERENCE = "voicemail_host";
    public static final String KEY_PASSWORD_PREFERENCE = "voicemail_password";
    public static final String KEY_PORT_PREFERENCE = "voicemail_port";
    public static final String KEY_SUBSCRIBER_ID_PREFERENCE = "subscriber_id";
    public static final String KEY_USERNAME_PREFERENCE = "voicemail_username";

    private static final String TAG = ApplicationSettings.class.getName();

    private EditTextPreference mUsernamePreference;
    private EditTextPreference mPasswordPreference;
    private EditTextPreference mHostPreference;
    private EditTextPreference mPortPreference;

    private SharedPreferences sharedPreferences;
    private ControllerAdapter controller = new ControllerAdapter(new DefaultController());
    private ListenVoicemailServiceBinder serviceBinder = new ListenVoicemailServiceBinder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.application_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get a reference to the preferences
        mUsernamePreference = (EditTextPreference)getPreferenceScreen().findPreference(KEY_USERNAME_PREFERENCE);
        mPasswordPreference = (EditTextPreference)getPreferenceScreen().findPreference(KEY_PASSWORD_PREFERENCE);
        mHostPreference = (EditTextPreference)getPreferenceScreen().findPreference(KEY_HOST_PREFERENCE);
        mPortPreference = (EditTextPreference)getPreferenceScreen().findPreference(KEY_PORT_PREFERENCE);

        serviceBinder.bind();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Setup the initial values
        mUsernamePreference.setSummary(sharedPreferences.getString(KEY_USERNAME_PREFERENCE, ""));
        mHostPreference.setSummary(sharedPreferences.getString(KEY_HOST_PREFERENCE, ""));
        mPortPreference.setSummary(sharedPreferences.getString(KEY_PORT_PREFERENCE, ""));

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        serviceBinder.unbind();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(KEY_USERNAME_PREFERENCE))
        {
            // if username changes, we need to retrieve the subscriber id for the username
            // we'll stop the service polling and update the id so that the service doesn't
            // retrieve new voicemails before the new subscriber id is set
            mUsernamePreference.setSummary(sharedPreferences.getString(key, ""));

            try
            {
                serviceBinder.getService().stopPolling();

                Long subscriberId = controller.getSubscriberIdFromUsername(this);
                ApplicationSettings.setSubscriberId(this, subscriberId);

                serviceBinder.getService().startPolling();
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Unable to get subscriber id from username", e);
            }
        }
        if(key.equals(KEY_PASSWORD_PREFERENCE))
        {
            try
            {
                serviceBinder.getService().stopPolling();

                Long subscriberId = controller.getSubscriberIdFromUsername(this);
                ApplicationSettings.setSubscriberId(this, subscriberId);

                serviceBinder.getService().startPolling();
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Unable to get subscriber id from username", e);
            }
        }
        else if(key.equals(KEY_HOST_PREFERENCE))
        {
            mHostPreference.setSummary(sharedPreferences.getString(key, ""));
            try
            {
                serviceBinder.getService().stopPolling();
                
                //subscriber id could be different on this new host
                Long subscriberId = controller.getSubscriberIdFromUsername(this);
                ApplicationSettings.setSubscriberId(this, subscriberId);
                
                serviceBinder.getService().startPolling();
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Unable to restart polling after host preference change");
            }
        }
        else if(key.equals(KEY_PORT_PREFERENCE))
        {
            mPortPreference.setSummary(sharedPreferences.getString(key, ""));
            try
            {
                serviceBinder.getService().stopPolling();
                
                //subscriber id could be different on the instance running on this new port
                Long subscriberId = controller.getSubscriberIdFromUsername(this);
                ApplicationSettings.setSubscriberId(this, subscriberId);
                
                serviceBinder.getService().startPolling();
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Unable to restart polling after host preference change");
            }
        }
    }

    /**
     * Gets the base location of the controller API.
     * 
     * @param context
     * @return API base location, e.g. {@code http://127.0.0.1:9091/api}
     */
    public static String getApi(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = prefs.getString(KEY_HOST_PREFERENCE, "");
        String host = hostname;

        // get IP for host
        try
        {
            InetAddress address = InetAddress.getByName(hostname);
            host = address.getHostAddress();
        }
        catch(UnknownHostException e)
        {
            Log.e(TAG, "Couldn't determine IP for host [" + hostname + "]", e);
        }

        String port = prefs.getString(KEY_PORT_PREFERENCE, "");
        return "http://" + host + ":" + port + "/api";
    }

    public static void setSubscriberId(Context context, Long subscriberId)
    {
        Log.d(TAG, "Set subscriber id to [" + subscriberId + "]");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SUBSCRIBER_ID_PREFERENCE, subscriberId);
        editor.commit();
    }

    /**
     * Gets the subscriber id for the currently configured username.
     * 
     * @param context
     * @return current subscriber id
     */
    public static Long getSubscriberId(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_SUBSCRIBER_ID_PREFERENCE, Long.valueOf(-1));
    }

    /**
     * Gets the currently configured username.
     * 
     * @param context
     * @return current username
     */
    public static String getUsername(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_USERNAME_PREFERENCE, ""); // TODO better default
    }
    
    /**
     * Gets the currently configured password.
     * 
     * @param context
     * @return current password
     */
    public static String getPassword(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_PASSWORD_PREFERENCE, ""); // TODO better default
    }
}
