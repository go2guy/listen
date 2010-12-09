package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;

import com.google.android.c2dm.C2DMessaging;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.sync.SyncAdapter;
import com.interact.listen.android.voicemail.sync.SyncSchedule;
import com.interact.listen.android.voicemail.widget.NumberPicker;
import com.interact.listen.android.voicemail.widget.NumberPickerDialog;

public class ApplicationSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
   
    private static final String TAG = Constants.TAG + "AppSettings";

    public static final String SYNC_EXTERNAL = "external_storage";

    private static final String CLEAR_CACHE = "clear_cache_pref";
    private static final String SYNC_INTERVAL_MINUTES = "sync_interval_minutes";
    private static final String SYNC_INTERVAL = "sync_interval_pref";
    private static final String SYNC_AUDIO = "sync_audio";
    private static final String NOTIFY_ENABLED = "notif_enabled";
    private static final String NOTIFY_VIBRATE = "notif_vibrate";
    private static final String NOTIFY_LIGHT = "notif_light";
    private static final String NOTIFY_RINGTONE = "notif_ringtone";
    private static final String DIAL_PREFIX = "dial_prefix";
    private static final String SYNC_SETTINGS = "accounts_sync_settings_key";
    private static final String RESET_PASSWORD = "pref_reset_password_key";

    private SharedPreferences sharedPreferences;
    private Preference clearCachePref;
    private Preference syncIntervalPref;
    private Preference syncSettingsPref;
    private Preference resetPasswordPref;
    
    private ClearCacheTask clearCacheTask;
    
    private OnSharedPreferenceChangeListener c2dmListener = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.v(TAG, "creating application settings");
        super.onCreate(savedInstanceState);

        clearCacheTask = null;
        
        addPreferencesFromResource(R.xml.application_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        OnPreferenceClickListener clickListener = new PreferenceClicked();
        
        clearCachePref = findPreference(CLEAR_CACHE);
        clearCachePref.setOnPreferenceClickListener(clickListener);
        
        syncIntervalPref = findPreference(SYNC_INTERVAL);
        syncIntervalPref.setOnPreferenceClickListener(clickListener);
        
        syncSettingsPref = findPreference(SYNC_SETTINGS);
        syncSettingsPref.setOnPreferenceClickListener(clickListener);
        
        resetPasswordPref = findPreference(RESET_PASSWORD);
        resetPasswordPref.setOnPreferenceClickListener(clickListener);
        
        EditTextPreference dialPrefix = (EditTextPreference)findPreference(DIAL_PREFIX);
        dialPrefix.setSummary(sharedPreferences.getString(DIAL_PREFIX, getString(R.string.pref_dial_prefix_summary)));
        dialPrefix.getEditText().addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        
        updateSyncIntevalSummary();
        
        c2dmListener = new C2DMEnabledListner();
    }
    
    private class C2DMEnabledListner implements OnSharedPreferenceChangeListener, Runnable
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key)
        {
            runOnUiThread(this);
        }
        
        @Override
        public void run()
        {
            updateSyncIntevalSummary();
        }
    }
    
    
    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "destroying application settings");
        if(clearCacheTask != null)
        {
            clearCacheTask.cancel(true);
            clearCacheTask = null;
        }
        if(c2dmListener != null)
        {
            C2DMessaging.unregisterEnabledListener(this, c2dmListener);
            c2dmListener = null;
        }
        super.onDestroy();
    }
    
    private int updateSyncIntevalSummary()
    {
        int interval = 0;
        
        if(C2DMessaging.isEnabled(getApplicationContext()))
        {
            syncIntervalPref.setSummary(R.string.pref_sync_c2dm_enabled);
            syncIntervalPref.setEnabled(false);
        }
        else
        {
            interval = getSyncIntervalMinutes(this);
            Log.v(TAG, "syn interval is " + interval);

            syncIntervalPref.setEnabled(true);
            int id = interval == 1 ? R.string.pref_sync_interval_detail_ns : R.string.pref_sync_interval_detail_ws;
            syncIntervalPref.setSummary(getString(id, interval));
        }
        
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
            else if(pref == syncSettingsPref)
            {
                Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{VoicemailProvider.AUTHORITY});
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
            else if(pref == resetPasswordPref)
            {
                AccountManager am = AccountManager.get(ApplicationSettings.this);
                Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
                for(Account account : accounts)
                {
                    am.updateCredentials(account, Constants.AUTHTOKEN_TYPE, null, ApplicationSettings.this, null, null);
                }
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
                    editor.putInt(SYNC_INTERVAL_MINUTES, value);
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
                if(clearCacheTask != null)
                {
                    Log.w(TAG, "currently a clear cache task");
                }
                clearCacheTask = new ClearCacheTask(ApplicationSettings.this);
                clearCacheTask.execute((Void[])null);
            }
            
        });
        
        return builder.create();
    }
    
    private static final class ClearCacheTask extends AsyncTask<Void, Void, Integer>
    {
        private Context mContext;
        private ProgressDialog progressDialog;
        private Object syncObject;
        
        public ClearCacheTask(Context context)
        {
            super();
            mContext = context;
            progressDialog = null;
            syncObject = new Object();
        }

        private Context getContext()
        {
            synchronized(syncObject)
            {
                return mContext;
            }
        }
        private Context clearContext()
        {
            Context c = null;
            synchronized(syncObject)
            {
                c = mContext;
                mContext = null;
            }
            return c;
        }
        
        @Override
        protected void onPreExecute()
        {
            Context c = getContext();
            if(c != null)
            {
                progressDialog = ProgressDialog.show(c, "", c.getString(R.string.clearing_cache_progress));
            }
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            onEnd();
        }

        @Override
        protected void onCancelled()
        {
            onEnd();
        }

        private void onEnd()
        {
            clearContext();
            if(progressDialog != null)
            {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
        
        @Override
        protected Integer doInBackground(Void... params)
        {
            Log.i(TAG, "clearing cache task starting");
            Context context = clearContext();
            
            if(context != null)
            {
                NotificationHelper.clearNotificationBar(context);

                VoicemailHelper.deleteVoicemails(context.getContentResolver(), null);
                try
                {
                    SyncAdapter.removeAccountInfo(context, null);
                }
                catch(Exception e)
                {
                    Log.e(TAG, "problem clearing account information", e);
                }
                SyncSchedule.syncRegular(context, null, true);
            }
            Log.i(TAG, "clearing cache task done");
            return 0;
        }
    }
    
    @Override
    protected void onPause()
    {
        Log.v(TAG, "pausing application settings");
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        C2DMessaging.unregisterEnabledListener(this, c2dmListener);
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "resuming application settings");
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        C2DMessaging.registerEnabledListener(this, c2dmListener);
        updateSyncIntevalSummary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        Log.v(TAG, "shared preference changed: " + key);
        
        if(SYNC_INTERVAL_MINUTES.equals(key))
        {
            SyncSchedule.updatePeriodicSync(this);
            updateSyncIntevalSummary();
        }
        else if(DIAL_PREFIX.equals(key))
        {
            Preference pref = findPreference(key);
            pref.setSummary(preferences.getString(key, getString(R.string.pref_dial_prefix_summary)));
        }
    }
    
    public static int getSyncIntervalMinutes(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(SYNC_INTERVAL_MINUTES, 15);
    }
    
    public static boolean isSyncAudio(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SYNC_AUDIO, false);
    }

    public static boolean isNotificationEnabled(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(NOTIFY_ENABLED, true);
    }
    
    public static boolean isVibrateEnabled(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(NOTIFY_VIBRATE, true);
    }

    public static boolean isLightEnabled(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(NOTIFY_LIGHT, true);
    }
    
    public static String getNotificationRing(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(NOTIFY_RINGTONE, null);
    }

    public static String getDialPrefix(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(DIAL_PREFIX, "");
    }
    
    public static boolean isExternalStorageEnabled(Context context)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SYNC_EXTERNAL, false);
    }
    
    public static void registerListener(Context context, OnSharedPreferenceChangeListener listener)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterListener(Context context, OnSharedPreferenceChangeListener listener)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

}
