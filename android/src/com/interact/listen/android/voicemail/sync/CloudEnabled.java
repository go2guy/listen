package com.interact.listen.android.voicemail.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

public class CloudEnabled
{
    public interface EnabledListener
    {
        public void updateEnabled(boolean enabled);
    }

    private static final String TAG = Constants.TAG + "CloudEnabled";
    
    private final WeakReference<Context> mContext;
    private final PrefObservable mObservable;
    private final PrefChanged mPrefListener;
    
    private volatile boolean mCurrentRegistered;
    private volatile boolean mCurrentEnabled;
    
    public CloudEnabled(Context context)
    {
        super();
        mContext = new WeakReference<Context>(context);
        mObservable = new PrefObservable();
        mPrefListener = new PrefChanged();
        mCurrentRegistered = false;
        mCurrentEnabled = false;
    }
    
    public boolean isEnabled()
    {
        if(mObservable.countObservers() == 0)
        {
            checkEnabled(false);
        }
        return mCurrentEnabled && mCurrentRegistered;
    }
    
    public void registerListener(EnabledListener listener)
    {
        mObservable.addObserver(new RegisteredObserver(listener));
    }
    
    public void unregisterListener(EnabledListener listener)
    {
        mObservable.deleteObserver(new RegisteredObserver(listener));
    }
    
    private void unregisterPrefListener()
    {
        Context context = mContext.get();
        if(context == null)
        {
            return;
        }

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        for(Account account : accounts)
        {
            final SharedPreferences prefs = CloudState.INSTANCE.getAccountMeta(context, account);
            prefs.unregisterOnSharedPreferenceChangeListener(mPrefListener);
        }

        CloudState.INSTANCE.getC2DMMeta(context).unregisterOnSharedPreferenceChangeListener(mPrefListener);
        
        context = null;
    }
    
    private void checkEnabled(boolean addListener)
    {
        Context context = mContext.get();
        if(context == null)
        {
            return;
        }

        int nEnabled = 0;
        int nTotal = 0;
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
        Authority[] authorities = Authority.values();
        for(Account account : accounts)
        {
            for(Authority authority : authorities)
            {
                ++nTotal;
                if(CloudState.INSTANCE.isAuthorityCloudSyncEnabled(context, account, authority))
                {
                    ++nEnabled;
                }
            }
            if(addListener)
            {
                final SharedPreferences pref = CloudState.INSTANCE.getAccountMeta(context, account);
                pref.registerOnSharedPreferenceChangeListener(mPrefListener);
            }
        }

        if(addListener)
        {
            final SharedPreferences pref = CloudState.INSTANCE.getC2DMMeta(context);
            pref.registerOnSharedPreferenceChangeListener(mPrefListener);
        }
        
        mCurrentRegistered = nEnabled != 0 && nEnabled == nTotal;
        mCurrentEnabled = CloudState.INSTANCE.isCloudSyncEnabled(context);

        Log.v(TAG, "Registered: " + mCurrentRegistered + " Enabled: " + mCurrentEnabled);
        
        final boolean enabled = mCurrentRegistered && mCurrentEnabled;
        mObservable.updateEnabled(enabled);
        mObservable.notifyObservers(enabled);
        
        context = null;
    }
    
    private static final class RegisteredObserver implements Observer
    {
        private final WeakReference<EnabledListener> mListener;

        public RegisteredObserver(EnabledListener listener)
        {
            mListener = new WeakReference<EnabledListener>(listener);
        }
        
        @Override
        public void update(Observable obs, Object data)
        {
            EnabledListener listener = mListener.get();
            if(listener != null)
            {
                listener.updateEnabled(((Boolean)data).booleanValue());
                listener = null;
            }
            else
            {
                obs.deleteObserver(this);
            }
        }
        
        @Override
        public boolean equals(Object o)
        {
            if(!(o instanceof RegisteredObserver))
            {
                return false;
            }
            
            final EnabledListener l1 = mListener.get();
            final EnabledListener l2 = ((RegisteredObserver)o).mListener.get();
            
            return l1 == null || l2 == null ? l1 == l2 : l1.equals(l2);
        }
        
        @Override
        public int hashCode()
        {
            final EnabledListener l = mListener.get();
            return l == null ? 0 : l.hashCode();
        }
    }
    
    private class PrefObservable extends Observable
    {
        private volatile boolean lastEnabled = false;
        
        @Override
        public void deleteObserver(Observer o)
        {
            final boolean unReg;
            synchronized(this)
            {
                if(this.countObservers() == 0)
                {
                    return;
                }
                super.deleteObserver(o);
                unReg = this.countObservers() == 0;
            }
            if(unReg)
            {
                unregisterPrefListener();
            }
        }
        
        @Override
        public void deleteObservers()
        {
            synchronized(this)
            {
                if(this.countObservers() == 0)
                {
                    return;
                }
                super.deleteObservers();
            }
            unregisterPrefListener();
        }
        
        @Override
        public void addObserver(Observer o)
        {
            final int prevCount;
            synchronized(this)
            {
                prevCount = this.countObservers();
                super.addObserver(o);
            }
            if(prevCount == 0)
            {
                checkEnabled(true);
            }
        }
        
        public synchronized void updateEnabled(boolean enabled)
        {
            if(lastEnabled != enabled)
            {
                lastEnabled = enabled;
                setChanged();
            }
        }
    }
    
    private class PrefChanged implements OnSharedPreferenceChangeListener
    {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            Log.v(TAG, "preference " + key + " changed");
            checkEnabled(false);
        }
        
    }
}
