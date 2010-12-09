package com.interact.listen.android.voicemail.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.SyncAdapter;

public class AccountRemovalActivity extends Activity
{
    private static final String TAG = Constants.TAG + "AccountRemoval";
    
    private AccountAuthenticatorResponse response = null;
    private String accountName = null;
    private RemoveAccountTask task = null;
        
    @Override
    protected void onCreate(Bundle savedBundle)
    {
        super.onCreate(savedBundle);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Bundle extras = getIntent().getExtras();
        
        accountName = extras.getString(AccountManager.KEY_ACCOUNT_NAME);
        response = (AccountAuthenticatorResponse)extras.get(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        
        Log.i(TAG, "onCreate('" + accountName + "')");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume('" + accountName + "')");
        showDialog(0);
        if(task == null)
        {
            task = new RemoveAccountTask();
            task.execute((Void[])null);
        }
    }
    
    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "onDestroy()");
        if(task != null)
        {
            task.cancel(true);
            task = null;
        }
        super.onDestroy();
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle)
    {
        ProgressDialog d = new ProgressDialog(this);
        d.setIndeterminate(true);
        d.setCancelable(false);
        d.setMessage(getString(R.string.remove_account_progress));
        return d;
    }
    
    private class RemoveAccountTask extends AsyncTask<Void, Void, Bundle>
    {

        @Override
        protected void onPostExecute(Bundle result)
        {
            Log.v(TAG, "onPostExecute()");
            onEnd(result);
            
            setResult(RESULT_OK);
            finish();
        }

        @Override
        protected void onCancelled()
        {
            Log.v(TAG, "onCancelled()");

            Bundle bundle = new Bundle();
            bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
            bundle.putInt(AccountManager.KEY_ERROR_CODE, -1);
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "Removal cancelled");

            onEnd(bundle);
            
            setResult(RESULT_CANCELED);
            finish();
        }

        private void onEnd(Bundle bundle)
        {
            try
            {
                dismissDialog(0);
            }
            catch(IllegalArgumentException e)
            {
                Log.w(TAG, "dialog not shown");
            }
            if(response != null)
            {
                response.onResult(bundle);
            }
        }
        
        @Override
        protected Bundle doInBackground(Void... params)
        {
            Log.v(TAG, "doInBackground('" + accountName + "')");

            Bundle bundle = new Bundle();
            
            bundle.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);

            Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(Constants.ACCOUNT_TYPE);
            Account account = null;
            for(Account acc : accounts)
            {
                if(acc.name.equals(accountName))
                {
                    account = acc;
                    break;
                }
            }
            if(account == null)
            {
                Log.e(TAG, "couln't find account " + accountName);
                bundle.putInt(AccountManager.KEY_ERROR_CODE, 0);
                bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "Account " + accountName + " not found.");
                return bundle;
            }
            
            VoicemailHelper.deleteVoicemails(getContentResolver(), accountName);
            try
            {
                SyncAdapter.removeAccountInfo(getApplicationContext(), account);
                Log.v(TAG, "done removing account");
            }
            catch(Exception e)
            {
                Log.e(TAG, "exception removing account info", e);
                bundle.putInt(AccountManager.KEY_ERROR_CODE, 1);
                bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "Unable to unregister with server.");
            }

            return bundle;
        }
    }

}
