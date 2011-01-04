package com.interact.listen.android.voicemail.contact;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.Log;

import com.interact.listen.android.voicemail.ApplicationSettings;
import com.interact.listen.android.voicemail.Constants;
import com.interact.listen.android.voicemail.NotificationHelper;

import java.util.ArrayList;

public class BatchOperation
{
    private static final String TAG = Constants.TAG + "Batch";

    private final Context mContext;
    private final ContentProviderClient mResolver;
    private final String mAccountName;
    private final String mOfficePrefix;
    private ArrayList<ContentProviderOperation> mOperations;

    public BatchOperation(Context context, ContentProviderClient resolver, String accountName)
    {
        mContext = context;
        mResolver = resolver;
        mOfficePrefix = ApplicationSettings.getDialPrefix(context);
        mAccountName = accountName;
        mOperations = new ArrayList<ContentProviderOperation>();
    }

    String getAccountName()
    {
        return mAccountName;
    }
    
    ContentProviderClient getResolver()
    {
        return mResolver;
    }
    
    String getDialString(String number)
    {
        return NotificationHelper.getDialString(mOfficePrefix, number);
    }
    
    void add(ContentProviderOperation cpo)
    {
        mOperations.add(cpo);
    }

    public int size()
    {
        return mOperations.size();
    }

    public void execute()
    {
        Log.d(TAG, "executing batch operations: " + mOperations.size());
        
        if (mOperations.size() == 0)
        {
            return;
        }

        try
        {
            mResolver.applyBatch(mOperations);
        }
        catch (final OperationApplicationException e1)
        {
            Log.e(TAG, "storing contact data failed", e1);
        }
        catch (final RemoteException e2)
        {
            Log.e(TAG, "storing contact data failed", e2);
        }
        mOperations.clear();
    }

    public String getString(int resID)
    {
        return mContext.getString(resID);
    }

}
