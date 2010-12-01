package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.c2dm.C2DMBaseReceiver;
import com.google.android.c2dm.C2DMessaging;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

public class C2DMReceiver extends C2DMBaseReceiver
{
    private static final String TAG = Constants.TAG + "C2DM";

    private static final String C2DM_SENDER = "interactincorporated@gmail.com";

    private static final String C2DM_ACCOUNT_EXTRA = "account_name";
    private static final String C2DM_MESSAGE_EXTRA = "message";
    private static final String C2DM_MESSAGE_SYNC = "sync-voicemails";

    public C2DMReceiver()
    {
        super(C2DM_SENDER);
    }

    @Override
    public void onError(Context context, String errorId)
    {
        Toast.makeText(context, "Messaging registration error: " + errorId, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onMessage(Context context, Intent intent)
    {
        String accountName = intent.getExtras().getString(C2DM_ACCOUNT_EXTRA);
        String message = intent.getExtras().getString(C2DM_MESSAGE_EXTRA);
        if(C2DM_MESSAGE_SYNC.equals(message))
        {
            if(accountName != null)
            {
                Log.i(TAG, "Messaging request received for account " + accountName);
                SyncSchedule.syncFull(context, true, accountName);
            }
        }
    }

    public static void refreshAppC2DMRegistrationState(Context context)
    {
        boolean autoSyncDesired = false;
        if(ContentResolver.getMasterSyncAutomatically())
        {
            AccountManager am = AccountManager.get(context);
            Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
            for(Account account : accounts)
            {
                if(ContentResolver.getIsSyncable(account, VoicemailProvider.AUTHORITY) > 0 &&
                    ContentResolver.getSyncAutomatically(account, VoicemailProvider.AUTHORITY))
                {
                    autoSyncDesired = true;
                    break;
                }
            }
        }

        boolean autoSyncEnabled = !C2DMessaging.getRegistrationId(context).equals("");

        if(autoSyncEnabled != autoSyncDesired)
        {
            Log.i(TAG, "System-wide desirability for Listen Voicemail auto sync has changed; " +
                       (autoSyncDesired ? "registering" : "unregistering") + " application with C2DM servers.");

            if(autoSyncDesired)
            {
                C2DMessaging.register(context, C2DM_SENDER);
            }
            else
            {
                C2DMessaging.unregister(context);
            }
        }
    }
}
