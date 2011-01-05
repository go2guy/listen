package com.interact.listen.android.voicemail.sync;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.RemoteException;
import android.util.Log;

import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.client.AccountInfo;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.contact.BatchOperation;
import com.interact.listen.android.voicemail.contact.ContactAddress;
import com.interact.listen.android.voicemail.contact.ListenContact;
import com.interact.listen.android.voicemail.contact.ListenContacts;

import java.io.IOException;
import java.util.Iterator;

public class SyncContactsAdapter extends AbstractCloudSyncAdapter
{
    private static final String TAG = Constants.TAG + "SyncContacts";

    public SyncContactsAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    @Override
    public void authoritySync(AccountInfo aInfo, SyncType syncType, Authority auth,
                              ContentProviderClient provider, SyncResult syncResult, SharedPreferences prefs)
        throws AuthorizationException, RemoteException, IOException
    {
        Log.i(TAG, "authority sync " + aInfo.getName() + ": " + syncType + " auth=" + auth.name());

        ListenContacts.insertContactsSettings(provider, aInfo.getName());
        
        ListenContacts serverContacts = new ListenContacts();
        if(!ClientUtilities.getEmailContacts(aInfo, serverContacts) || isInterrupted())
        {
            Log.w(TAG, "error getting email contacts or interrupted");
            return;
        }
        if(!ClientUtilities.getNumberContacts(aInfo, serverContacts) || isInterrupted())
        {
            Log.w(TAG, "error getting all of the phone number contacts or interrupted");
            return;
        }
        
        for(ListenContact contact : serverContacts.getContacts())
        {
            Log.d(TAG, "server " + contact);
        }
        
        ListenContacts localContacts = new ListenContacts();
        localContacts.add(provider, aInfo.getName());
        
        final Iterator<ListenContact> lIter = localContacts.getContacts().iterator();
        final Iterator<ListenContact> sIter = serverContacts.getContacts().iterator();
        ListenSyncIter<ListenContact> cIter = new ListenSyncIter<ListenContact>(lIter, sIter);
        
        BatchOperation ops = new BatchOperation(getContext(), provider, aInfo.getName());
        
        while(cIter.next())
        {
            if(cIter.isMissingFromLocal())
            {
                ListenContacts.insert(cIter.getServer(), ops);
            }
            else if(cIter.isMissingFromServer())
            {
                ListenContacts.remove(cIter.getLocal(), ops);
            }
            else if(!cIter.getLocal().isDeleted())
            {
                if(!cIter.getLocal().equals(cIter.getServer()))
                {
                    ListenContacts.update(cIter.getLocal(), cIter.getServer(), ops);
                }
                
                final Iterator<ContactAddress> laIter = cIter.getLocal().getAddresses().iterator();
                final Iterator<ContactAddress> saIter = cIter.getServer().getAddresses().iterator();
                ListenSyncIter<ContactAddress> aIter = new ListenSyncIter<ContactAddress>(laIter, saIter);
                
                while(aIter.next())
                {
                    if(aIter.isMissingFromLocal())
                    {
                        ListenContacts.insert(cIter.getLocal(), aIter.getServer(), ops);
                    }
                    else if(aIter.isMissingFromServer())
                    {
                        ListenContacts.remove(cIter.getLocal(), aIter.getLocal(), ops);
                    }
                    else if(!aIter.getLocal().equals(aIter.getServer()))
                    {
                        ListenContacts.update(cIter.getLocal(), aIter.getLocal(), aIter.getServer(), ops);
                    }
                }
            }
            
            if(isInterrupted())
            {
                Log.i(TAG, "interrupted in the middle of a contact sync");
                return;
            }
            
            if(ops.size() >= 50)
            {
                ops.execute();
            }
        }
        
        ops.execute();
        
        Log.i(TAG, "synced contacts");
    }
}
