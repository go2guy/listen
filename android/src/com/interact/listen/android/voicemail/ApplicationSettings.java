package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.sync.SyncSchedule;
import com.interact.listen.android.voicemail.widget.NumberPicker;
import com.interact.listen.android.voicemail.widget.NumberPickerDialog;

public class ApplicationSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
   
    private static final String TAG = Constants.TAG + "AppSettings";

    private static final String CLEAR_CACHE = "clear_cache_pref";
    private static final String SYNC_INTERVAL = "sync_interval_pref";
    private static final String SYNC_AUDIO = "sync_audio";

    private SharedPreferences sharedPreferences;
    private Preference clearCachePref;
    private Preference syncIntervalPref;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "creating application settings");
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.application_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        OnPreferenceClickListener clickListener = new PreferenceClicked();
        
        clearCachePref = findPreference(CLEAR_CACHE);
        clearCachePref.setOnPreferenceClickListener(clickListener);
        
        syncIntervalPref = findPreference(SYNC_INTERVAL);
        syncIntervalPref.setOnPreferenceClickListener(clickListener);

        updateSyncIntevalSummary();
    }

    private int updateSyncIntevalSummary()
    {
        int interval = SyncSchedule.getSyncIntervalMinutes(this);
        Log.v(TAG, "syn interval is " + interval);
        
        int id = interval == 1 ? R.string.pref_sync_interval_detail_ns : R.string.pref_sync_interval_detail_ws;
        syncIntervalPref.setSummary(getString(id, interval));

        return interval;
    }

    private class PreferenceClicked implements OnPreferenceClickListener
    {
        @Override
        public boolean onPreferenceClick(Preference pref)
        {
            Dialog d = null;
            if(pref == clearCachePref)
            {
                d = createClearCacheDialog();
            }
            else if(pref == syncIntervalPref)
            {
                d = createUpdateSyncIntervalDialog();
            }
            if(d != null && !d.isShowing())
            {
                d.show();
            }
            return d != null;
        }
    }
    
    private Dialog createUpdateSyncIntervalDialog()
    {
        NumberPickerDialog.Builder builder = new NumberPickerDialog.Builder(this);
        
        builder.setCallBack(new NumberPickerDialog.OnNumberSetListener()
        {
            @Override
            public void onNumberSet(NumberPicker view, int value)
            {
                Log.i(TAG, "new pool interval reported back from dialog: " + value);
                if(value > 0)
                {
                    Editor editor = sharedPreferences.edit();
                    editor.putInt(SyncSchedule.PREFERENCES_INT_SYNC_INTERVAL_MINUTES, value);
                    editor.commit();
                }
            }
        });

        builder.setInitialValue(updateSyncIntevalSummary());
        builder.setRange(1, 999);
        builder.setButtons(R.string.dialog_sync_interval_confirm, R.string.dialog_sync_interval_cancel);
        builder.setDetails(R.string.dialog_sync_interval_details);
        builder.setTitleResIDs(R.string.pref_sync_interval_detail_ns, R.string.pref_sync_interval_detail_ws);

        return builder.create();
    }
    
    private Dialog createClearCacheDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(true);
        builder.setTitle(R.string.dialog_clear_cache_title);
        builder.setMessage(R.string.dialog_clear_cache_details);
        builder.setNegativeButton(R.string.dialog_clear_cache_cancel, null);
        
        builder.setPositiveButton(R.string.dialog_clear_cache_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Log.i(TAG, "request to clear cache");
                VoicemailHelper.refreshCache(getContentResolver());
                Bundle rSyncBundle = new Bundle();
                AccountManager am = AccountManager.get(ApplicationSettings.this);
                Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
                for (Account account : accounts)
                {
                    Log.i(TAG, "requesting sync on " + account.name);
                    ContentResolver.requestSync(account, VoicemailProvider.AUTHORITY, rSyncBundle);
                }
            }
            
        });
        
        return builder.create();
    }
    
    @Override
    protected void onPause()
    {
        Log.v(TAG, "pausing application settings");
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "resuming application settings");
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "destroying application settings");
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        Log.v(TAG, "shared preference changed: " + key);
        
        if(SyncSchedule.PREFERENCES_INT_SYNC_INTERVAL_MINUTES.equals(key))
        {
            SyncSchedule.updatePeriodicSync(this);
            updateSyncIntevalSummary();
        }
    }
    
    public static boolean isSyncAudio(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SYNC_AUDIO, true);
    }

}
