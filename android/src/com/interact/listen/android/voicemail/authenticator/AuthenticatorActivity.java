package com.interact.listen.android.voicemail.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.R;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.client.OnAuthenticateHandler;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class AuthenticatorActivity extends AccountAuthenticatorActivity implements OnAuthenticateHandler
{
    private static final String TAG = Constants.TAG + "AuthActivity";

    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
    public static final String PARAM_HOST = "host";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";

    private AccountManager mAccountManager;
    private Thread mAuthThread;
    private String mAuthtoken;
    private String mAuthtokenType;

    private Uri mHost;
    private String mUsername;
    private String mPassword;
    
    private TextView mMessage;
    private EditText mUsernameEdit;
    private EditText mHostEdit;
    private EditText mPasswordEdit;

    private boolean mConfirmCredentials = false; // set if just checking credentials
    private boolean mRequestNewAccount = false;

    private final Handler mHandler = new Handler();
    
    private boolean dialogShown = false;
    
    @Override
    public void onCreate(Bundle bundle)
    {
        Log.i(TAG, "create: " + bundle);
        super.onCreate(bundle);

        mAccountManager = AccountManager.get(this);
        
        final Intent intent = getIntent();

        String host = intent.getStringExtra(PARAM_HOST);
        if(TextUtils.isEmpty(host))
        {
            mHost = null;
        }
        else
        {
            mHost = Uri.parse(host);
        }
        mUsername = intent.getStringExtra(PARAM_USERNAME);

        mAuthtokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        mRequestNewAccount = mUsername == null || mHost == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        Log.i(TAG, "request new: " + mRequestNewAccount);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_view);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMessage = (TextView)findViewById(R.id.message);
        mHostEdit = (EditText)findViewById(R.id.host_edit);
        mUsernameEdit = (EditText)findViewById(R.id.username_edit);
        mPasswordEdit = (EditText)findViewById(R.id.password_edit);

        if(mHost == null)
        {
            mHostEdit.setText("http://");

            Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account account : accounts)
            {
                final String cHost = mAccountManager.getUserData(account, Authenticator.HOST_DATA);
                if(!TextUtils.isEmpty(cHost))
                {
                    mHostEdit.setText(cHost);
                    if(mRequestNewAccount)
                    {
                        mHostEdit.setEnabled(false);
                        mUsernameEdit.requestFocus();
                    }
                    break;
                }
            }
        }
        else
        {
            mHostEdit.setText(mHost.toString());
        }
        mUsernameEdit.setText(mUsername);
        mMessage.setText(getMessage());
        
        if(!mRequestNewAccount)
        {
            mUsernameEdit.setEnabled(false);
            mHostEdit.setEnabled(false);
            mPasswordEdit.requestFocus();
        }
    }
    
    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "destroying authenticator activity");
        hideProgress();
        super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.ui_authenticating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface dialog)
            {
                Log.i(TAG, "dialog cancel");
                if(mAuthThread != null)
                {
                    mAuthThread.interrupt();
                    finish();
                }
            }
        });
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button
     * 
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view)
    {
        if(mRequestNewAccount)
        {
            String host = mHostEdit.getText().toString();
            if (TextUtils.isEmpty(host))
            {
                mHost = null;
            }
            else
            {
                try
                {
                    URI tURI = new URI(host);
                    URL tURL = tURI.toURL();
                    Log.i(TAG, "verified host: " + tURL.toString());
                }
                catch (URISyntaxException se)
                {
                    Log.i(TAG, "unable to verify URI", se);
                    mMessage.setText(R.string.login_activity_loginfail_text_host);
                    return;
                }
                catch(MalformedURLException me)
                {
                    Log.i(TAG, "malformed url", me);
                    mMessage.setText(R.string.login_activity_loginfail_text_host);
                    return;
                }
                catch(Exception e)
                {
                    Log.i(TAG, "unable to verify host", e);
                    mMessage.setText(R.string.login_activity_loginfail_text_host);
                    return;
                }
                mHost = Uri.parse(host);
            }
        }
        mUsername = mUsernameEdit.getText().toString();
        mPassword = mPasswordEdit.getText().toString();
        if(TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword) || mHost == null)
        {
            mMessage.setText(getMessage());
        }
        else
        {
            showProgress();
            mAuthThread = ClientUtilities.attemptAuth(mHost, mUsername, mPassword, mHandler, this);
        }
    }

    /**
     * Called when response is received from the server for confirm credentials request
     * 
     * @param the user id
     * @param AccountManager error code.
     */
    protected void finishConfirmCredentials(long result, int errorCode)
    {
        Log.i(TAG, "finish confirm credentials: " + result);
        
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mPassword);
        mAccountManager.setUserData(account, Authenticator.HOST_DATA, mHost == null ? null : mHost.toString());
        mAccountManager.setUserData(account, Authenticator.ID_DATA, Long.toString(result));
        
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result > 0);
        if(result <= 0 && errorCode != 0)
        {
            intent.putExtra(AccountManager.KEY_ERROR_CODE, errorCode);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when response is received from the server for authentication request
     */
    protected void finishLogin(long id)
    {
        Log.i(TAG, "finish login: " + id);
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);

        if(mRequestNewAccount)
        {
            Bundle userData = new Bundle();
            userData.putString(Authenticator.HOST_DATA, mHost.toString());
            userData.putString(Authenticator.ID_DATA, Long.toString(id));
            mAccountManager.addAccountExplicitly(account, mPassword, userData);

            SyncSchedule.accountAdded(this, account);
        }
        else
        {
            mAccountManager.setPassword(account, mPassword);
            
            String cIdStr = mAccountManager.getUserData(account, Authenticator.ID_DATA);
            if(TextUtils.isEmpty(cIdStr))
            {
                Log.i(TAG, "user id not set, setting");
                mAccountManager.setUserData(account, Authenticator.ID_DATA, Long.toString(id));
            }
            else
            {
                try
                {
                    long cID = Long.parseLong(cIdStr);
                    if (cID <= 0 || (id >= 0 && cID != id))
                    {
                        String nIdStr = Long.toString(id);
                        Log.i(TAG, "Changing ID from " + cIdStr + " to " + nIdStr);
                        mAccountManager.setUserData(account, Authenticator.ID_DATA, nIdStr);
                    }
                }
                catch(Exception e)
                {
                    Log.e(TAG, "current id " + cIdStr + " not long!", e);
                }
            }
        }
        final Intent intent = new Intent();
        mAuthtoken = ClientUtilities.getBase64EncodedString(mPassword);
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        if(mAuthtokenType != null && mAuthtokenType.equals(Constants.AUTHTOKEN_TYPE))
        {
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthtoken);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void hideProgress()
    {
        if(!dialogShown)
        {
            Log.v(TAG, "progress dialog isn't shown");
        }
        else
        {
            dialogShown = false;
            try
            {
                dismissDialog(0);
            }
            catch(IllegalArgumentException e)
            {
                Log.w(TAG, "appears progress dialog was never shown", e);
            }
        }
    }

    /**
     * Called when the authentication process completes
     */
    @Override
    public void onAuthenticationResult(long result, int errorCode)
    {
        hideProgress();

        if(result > 0)
        {
            Log.i(TAG, "auth succesfull");
            if(!mConfirmCredentials)
            {
                finishLogin(result);
            }
            else
            {
                finishConfirmCredentials(result, errorCode);
            }
        }
        else
        {
            Log.e(TAG, "authentication failed: " + errorCode);
            if(errorCode == AccountManager.ERROR_CODE_NETWORK_ERROR)
            {
                mMessage.setText(getText(R.string.login_activity_loginfail_text_network));
            }
            else if(mRequestNewAccount)
            {
                mMessage.setText(getText(R.string.login_activity_loginfail_text_all));
            }
            else
            {
                mMessage.setText(getText(R.string.login_activity_loginfail_text_password));
            }
        }
    }

    private CharSequence getMessage()
    {
        if(TextUtils.isEmpty(mUsername) || mHost == null)
        {
            return getText(R.string.login_activity_newaccount_text);
        }
        if(TextUtils.isEmpty(mPassword))
        {
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }

    protected void showProgress()
    {
        dialogShown = true;
        showDialog(0);
    }
}
