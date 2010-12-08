package com.interact.listen.android.voicemail.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.SyncAdapter;

public class Authenticator extends AbstractAccountAuthenticator
{
    public static final String ID_DATA = "user_id";
    public static final String HOST_DATA = "listen_server";
    
    private static final String TAG = Constants.TAG + "Authenticator";
    
    private final Context mContext;
    
    public Authenticator(Context context)
    {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options)
    {
        Log.v(TAG, "add account request to authenticator");
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
    {
        Log.v(TAG, "confirm credentials: " + account.name);
        
        final AccountManager am = AccountManager.get(mContext);
        String host = am.getUserData(account, AuthenticatorActivity.PARAM_HOST);

        if(options != null && options.containsKey(AccountManager.KEY_PASSWORD))
        {
            final Uri uri = TextUtils.isEmpty(host) ? null : Uri.parse(host);
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            final boolean verified = onlineConfirmPassword(uri, am, account, password);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_HOST, host);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
    {
        Log.i(TAG, "edit properties not supported");
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
                               Bundle loginOptions)
    {
        Log.v(TAG, "get auth token " + account.name);
        if(!authTokenType.equals(Constants.AUTHTOKEN_TYPE))
        {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        final AccountManager am = AccountManager.get(mContext);
        final String host = am.getUserData(account, HOST_DATA);
        final String password = am.getPassword(account);
        if(password != null)
        {
            final boolean verified = onlineConfirmPassword(host == null ? null : Uri.parse(host), am, account, password);
            if(verified)
            {
                String authToken = ClientUtilities.getBase64EncodedString(password);
                Log.v(TAG, "verified account " + account.name);
                
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
        }
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_HOST, host);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_HOST, host);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType)
    {
        if(authTokenType.equals(Constants.AUTHTOKEN_TYPE))
        {
            return mContext.getString(R.string.authLabel);
        }
        return null;

    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
    {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    private boolean onlineConfirmPassword(Uri host, AccountManager am, Account account, String password)
    {
        final long id = ClientUtilities.authenticate(host, account.name, password, null, null);
        if(id <= 0)
        {
            return false;
        }
        String nIdStr = Long.toString(id);
        String idStr = am.getUserData(account, ID_DATA);
        if(idStr == null)
        {
            Log.i(TAG, "updating account " + account.name + " id to " + nIdStr + " because it wasn't set");
            am.setUserData(account, ID_DATA, nIdStr);
        }
        if(idStr == null || !idStr.equals(nIdStr))
        {
            Log.i(TAG, "updating account " + account.name + " id from " + idStr + " to " + nIdStr);
            am.setUserData(account, ID_DATA, nIdStr);
        }
        return true;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
                                    Bundle loginOptions)
    {
        Log.v(TAG, "updating credentials for " + account.name);
        final AccountManager am = AccountManager.get(mContext);
        String host = am.getUserData(account, HOST_DATA);

        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_HOST, host);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, false);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException
    {
        Log.v(TAG, "removing account " + account.name);
        VoicemailHelper.deleteVoicemails(mContext.getContentResolver(), account.name);
        try
        {
            SyncAdapter.removeAccountInfo(mContext, account);
        }
        catch(Exception e)
        {
            Log.e(TAG, "exception removing account info", e);
            throw new NetworkErrorException("clearing account information", e);
        }
        return super.getAccountRemovalAllowed(response, account);
    }
}
