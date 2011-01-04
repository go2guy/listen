package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.client.ServerRegistrationInfo;

import java.util.ArrayList;
import java.util.List;

enum CloudState
{
    INSTANCE;
    
    private static final String PROP_SENDER_STR      = "c2dm_sender";
    private static final String PROP_ENABLED_BOOL    = "c2dm_enabled";
    private static final String PROP_REG_ID_STR      = "c2dm_registration_id";
    private static final String PROP_PENDING_REG_MS  = "c2dm_pending_query";
    private static final String PROP_ACC_IS_REG_BOOL = "account_registered";
    
    private static final long STALE_PENDING = 1000 * 60 * 2;
    
    private static final String TAG = Constants.TAG + "CloudState";
    
    public final class UpdateState
    {
        private boolean needServerReg;
        private String senderID;
        private String registrationID;
        private List<Authority> enableAuthorities;
        private List<Authority> disableAuthorities;
        
        private UpdateState()
        {
            needServerReg = false;
            senderID = "";
            registrationID = "";
            enableAuthorities = new ArrayList<Authority>();
            disableAuthorities = new ArrayList<Authority>();
        }
        
        public boolean isServerRegistrationNeeded()
        {
            return needServerReg || !enableAuthorities.isEmpty() || !disableAuthorities.isEmpty();
        }
        public String getSenderID()
        {
            return senderID;
        }
        public String getRegistrationID()
        {
            return registrationID;
        }
        public Authority[] getEnableAuthorities()
        {
            return enableAuthorities.toArray(new Authority[enableAuthorities.size()]);
        }
        public Authority[] getDisableAuthorities()
        {
            return disableAuthorities.toArray(new Authority[disableAuthorities.size()]);
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[UpdateState reg: ").append(needServerReg).append(" sender: '").append(senderID);
            sb.append("' registration: '").append(registrationID).append("'");
            appendAuthorities(sb, " enable: ", enableAuthorities);
            appendAuthorities(sb, " disable: ", disableAuthorities);
            return sb.toString();
        }
        
        private void appendAuthorities(StringBuilder sb, String head, List<Authority> auths)
        {
            sb.append(head);
            if(auths.isEmpty())
            {
                sb.append("None");
            }
            else
            {
                for(Authority auth : auths)
                {
                    sb.append(auth.name()).append(',');
                }
                sb.deleteCharAt(sb.length() - 1);
            }
        }
    }

    synchronized void reportErrored(Context context)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        c2dmPrefs.edit().putLong(PROP_PENDING_REG_MS, 0L).commit();
    }

    synchronized void reportRegistered(Context context, String registrationId)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        Editor editor = c2dmPrefs.edit();
        editor.putLong(PROP_PENDING_REG_MS, 0L);
        editor.putString(PROP_REG_ID_STR, registrationId == null ? "" : registrationId);
        editor.commit();
    }

    void reportUnregistered(Context context)
    {
    }

    public synchronized String getSenderId(Context context)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        return c2dmPrefs.getString(PROP_SENDER_STR, "");
    }
    
    SharedPreferences getAccountMeta(Context context, Account account)
    {
        return context.getSharedPreferences("cloud_state:" + account.name, Context.MODE_PRIVATE);
    }

    SharedPreferences getC2DMMeta(Context context)
    {
        return context.getSharedPreferences("cloud_state", Context.MODE_PRIVATE);
    }

    void clearMeta(Context context, Account account)
    {
        final SharedPreferences accountPrefs = getAccountMeta(context, account);
        accountPrefs.edit().clear().commit();
    }
    
    void clearMeta(Context context)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        c2dmPrefs.edit().clear().commit();
    }
    
    boolean isCloudSyncEnabled(Context context)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        
        final boolean enabled = c2dmPrefs.getBoolean(PROP_ENABLED_BOOL, false);
        final String regID = c2dmPrefs.getString(PROP_REG_ID_STR, "");
        final String senderID = c2dmPrefs.getString(PROP_SENDER_STR, "");

        Log.v(TAG, "cloud enabled: " + enabled + " sender: " + senderID + " registration: " + regID);
        
        return enabled && regID.length() != 0 && senderID.length() != 0;
    }
    
    boolean isAuthorityCloudSyncEnabled(Context context, Account account, Authority authority)
    {
        final SharedPreferences accountPrefs = getAccountMeta(context, account);
        final boolean enabled = accountPrefs.getBoolean(makeRegisteredProperty(authority), false);
        Log.v(TAG, "account " + account.name + "-" + authority.name() + " enabled: " + enabled);
        return enabled;
    }
    
    boolean isCloudSyncActive(Context context, Account account, Authority authority)
    {
        return isCloudSyncEnabled(context) && isAuthorityCloudSyncEnabled(context, account, authority);
    }
    
    boolean isServerRegistrationCheckNeeded(Context context, Account account)
    {
        final SharedPreferences c2dmPrefs = getC2DMMeta(context);
        return c2dmPrefs.getString(PROP_SENDER_STR, "").length() == 0;
    }
    
    UpdateState updateServerRegistration(Context context, ServerRegistrationInfo info, Account account)
    {
        UpdateState state = new UpdateState();

        final SharedPreferences c2dmPrefs = getC2DMMeta(context);

        state.senderID = c2dmPrefs.getString(PROP_SENDER_STR, "");
        state.registrationID = c2dmPrefs.getString(PROP_REG_ID_STR, "");
        
        final boolean newSender = !state.senderID.equals(info.getSenderId());
        final boolean newEnabled = c2dmPrefs.getBoolean(PROP_ENABLED_BOOL, false) != info.isEnabled();
        final boolean newReg = !state.registrationID.equals(info.getRegistrationId());

        if(newSender || newEnabled)
        {
            Editor c2dmEditor = c2dmPrefs.edit();

            if(newEnabled)
            {
                c2dmEditor.putBoolean(PROP_ENABLED_BOOL, info.isEnabled());
            }
            if(newSender)
            {
                c2dmEditor.putString(PROP_SENDER_STR, info.getSenderId());
                c2dmEditor.putLong(PROP_PENDING_REG_MS, 0L);
            }
            
            if(newSender && info.getSenderId().length() == 0)
            {
                c2dmEditor.putString(PROP_REG_ID_STR, "");
                state.registrationID = "";

                CloudRegistration.unregister(context);
                c2dmEditor.putLong(PROP_PENDING_REG_MS, 0L);
            }
            state.senderID = info.getSenderId();
            
            c2dmEditor.commit();
        }

        if(state.registrationID.length() == 0 && state.senderID.length() > 0 && isAnyAutoSyncDesired(context))
        {
            long pendingMS = c2dmPrefs.getLong(PROP_PENDING_REG_MS, 0L);
            long now = System.currentTimeMillis();
            if(pendingMS == 0 || pendingMS + STALE_PENDING <= now)
            {
                CloudRegistration.register(context, state.senderID);
                c2dmPrefs.edit().putLong(PROP_PENDING_REG_MS, now).commit();
            }
            else
            {
                Log.v(TAG, "Not performing register because current registration");
            }
        }

        state.needServerReg = newReg;

        syncLocalRegistered(context, account, info, state.registrationID.length() == 0);
        setUpdateStateAuthorities(state, context, account);

        Log.v(TAG, state.toString());
        
        return state;
    }
    
    void commitUpdateState(UpdateState state, Context context, Account account)
    {
        final SharedPreferences accountPrefs = getAccountMeta(context, account);
        final Editor accountEditor = accountPrefs.edit();
        
        commitUpdateState(state.enableAuthorities, accountPrefs, accountEditor, true);
        commitUpdateState(state.disableAuthorities, accountPrefs, accountEditor, false);

        accountEditor.commit();
    }

    private void commitUpdateState(List<Authority> auths, SharedPreferences accountPrefs, Editor accountEditor, boolean enable)
    {
        for(Authority auth : auths)
        {
            final String prop = makeRegisteredProperty(auth);
            final boolean lReg = accountPrefs.getBoolean(prop, false);
            if(lReg != enable)
            {
                accountEditor.putBoolean(prop, enable);
            }
        }
    }
    
    private void syncLocalRegistered(Context context, Account account, ServerRegistrationInfo info, boolean forceUnreg)
    {
        final SharedPreferences accountPrefs = getAccountMeta(context, account);

        Editor accountEditor = accountPrefs.edit();
        final Authority[] authorities = Authority.values();
        for(Authority auth : authorities)
        {
            final String prop = makeRegisteredProperty(auth);
            final boolean lReg = accountPrefs.getBoolean(prop, false);
            
            boolean update = false;
            if(forceUnreg && lReg)
            {
                update = false;
            }
            else if(info.isRegistered(auth) != lReg)
            {
                update = !lReg;
            }
            else
            {
                continue;
            }
            accountEditor.putBoolean(prop, update);
        }
        accountEditor.commit();
    }
    
    private void setUpdateStateAuthorities(UpdateState state, Context context, Account account)
    {
        final SharedPreferences sp = getAccountMeta(context, account);
        
        final Authority[] authorities = Authority.values();
        if(ContentResolver.getMasterSyncAutomatically())
        {
            for(Authority auth : authorities)
            {
                final boolean isReg = sp.getBoolean(makeRegisteredProperty(auth), false);
                if(ContentResolver.getIsSyncable(account, auth.get()) > 0 &&
                    ContentResolver.getSyncAutomatically(account, auth.get()))
                {
                    if(!isReg)
                    {
                        state.enableAuthorities.add(auth);
                    }
                }
                else
                {
                    if(isReg)
                    {
                        state.disableAuthorities.add(auth);
                    }
                }
            }
        }
        else
        {
            for(Authority auth: authorities)
            {
                state.disableAuthorities.add(auth);
            }
        }
    }
    
    private static boolean isAnyAutoSyncDesired(Context context)
    {
        if(ContentResolver.getMasterSyncAutomatically())
        {
            final String[] authorities = Authority.getAuthorities();
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);

            for(Account account : accounts)
            {
                for(String auth : authorities)
                {
                    if(ContentResolver.getIsSyncable(account, auth) > 0 &&
                        ContentResolver.getSyncAutomatically(account, auth))
                    {
                        Log.v(TAG, "account " + account.name + " authority " + auth + " desires auto sync");
                        return true;
                    }
                }
            }
        }
        Log.v(TAG, "no auto sync desired");
        return false;
    }

    private static String makeRegisteredProperty(Authority authority)
    {
        return PROP_ACC_IS_REG_BOOL + "-" + authority.name();
    }

}
