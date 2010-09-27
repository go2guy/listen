package com.interact.listen.android.voicemail;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class ApplicationSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
    private static final String TAG = "ListenVoicemailApplicationSettings";
    public static final String KEY_HOST_PREFERENCE = "voicemail_host";
    public static final String KEY_PASSWORD_PREFERENCE = "voicemail_password";
    public static final String KEY_PORT_PREFERENCE = "voicemail_port";
    public static final String KEY_SUBSCRIBER_ID_PREFERENCE = "subscriber_id";
    public static final String KEY_USERNAME_PREFERENCE = "voicemail_username";

    private EditTextPreference mUsernamePreference;
    private EditTextPreference mPasswordPreference;
    private EditTextPreference mHostPreference;
    private EditTextPreference mPortPreference;
    
    private SharedPreferences sharedPreferences;

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
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Setup the initial values
        mUsernamePreference.setSummary("Current value is " + sharedPreferences.getString(KEY_USERNAME_PREFERENCE, ""));
        mHostPreference.setSummary("Current value is " + sharedPreferences.getString(KEY_HOST_PREFERENCE, ""));
        mPortPreference.setSummary("Current value is " + sharedPreferences.getString(KEY_PORT_PREFERENCE, ""));

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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        // Let's do something a preference value changes
        if(key.equals(KEY_USERNAME_PREFERENCE))
        {
            mUsernamePreference.setSummary("Current value is " + sharedPreferences.getString(key, ""));
        }
        else if(key.equals(KEY_HOST_PREFERENCE))
        {
            mHostPreference.setSummary("Current value is " + sharedPreferences.getString(key, ""));
        }
        else if(key.equals(KEY_PORT_PREFERENCE))
        {
            mPortPreference.setSummary("Current value is " + sharedPreferences.getString(key, ""));
        }
    }
}
