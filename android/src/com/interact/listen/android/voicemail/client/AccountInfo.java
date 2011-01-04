package com.interact.listen.android.voicemail.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.net.Uri;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.authenticator.Authenticator;

import java.io.IOException;

public final class AccountInfo
{
    private final Account account;
    private final Uri host;
    private final long userID;
    private final String authToken;

    private AccountInfo(Account account, Uri host, long userID, String authToken)
    {
        this.account = account;
        this.host = host;
        this.userID = userID;
        this.authToken = authToken;
    }

    public Account getAccount()
    {
        return account;
    }

    public Uri getHost()
    {
        return host;
    }

    public long getUserID()
    {
        return userID;
    }

    public String getAuthToken()
    {
        return authToken;
    }
    
    public String getName()
    {
        return account.name;
    }
    
    public static AccountInfo getAccountInfo(AccountManager manager, Account account)
        throws AuthenticatorException, OperationCanceledException, IOException
    {
        String token = manager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
        if(token == null)
        {
            throw new AuthenticatorException("unable to authenticate");
        }

        String uId = manager.getUserData(account, Authenticator.ID_DATA);
        String hStr = manager.getUserData(account, Authenticator.HOST_DATA);
        if (hStr == null || uId == null)
        {
            manager.invalidateAuthToken(Constants.ACCOUNT_TYPE, token);
            throw new AuthenticatorException("host and/or user I.D. not set");
        }
        
        Log.i(Constants.TAG, "Authorized " + hStr + " user ID " + uId);

        Uri host = Uri.parse(hStr);
        long userID = Long.parseLong(uId);
        
        return new AccountInfo(account, host, userID, token);
    }
}
