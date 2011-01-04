package com.interact.listen.android.voicemail;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.interact.listen.android.voicemail.WavConversion.Type;
import com.interact.listen.android.voicemail.authenticator.Authenticator;
import com.interact.listen.android.voicemail.client.AuthorizationException;
import com.interact.listen.android.voicemail.client.ClientUtilities;
import com.interact.listen.android.voicemail.provider.VoicemailHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;

public class DownloadRunnable implements Runnable, Comparable<DownloadRunnable>
{
    public interface OnProgressUpdate
    {
        void onProgressUpdate(int percent);
    }
    
    private static final String TAG = Constants.TAG + "DownloadRunnable";

    private Context context;
    private ContentResolver resolver;
    private Account account;
    private String authToken;
    private Voicemail voicemail;
    private AccountManager accountManager;
    private ContentProviderClient provider;
    private OnProgressUpdate progressListener;
    private WavConversion.Type type;
    
    public DownloadRunnable(Context context, Voicemail voicemail)
    {
        this.context = context;
        this.resolver = context.getContentResolver();
        this.account = null;
        this.authToken = null;
        this.voicemail = voicemail;
        this.accountManager = AccountManager.get(context);
        this.provider = null;
        this.progressListener = null;
        this.type = WavConversion.Type.UNKNOWN;
    }

    public void setAccount(Account acc, String token)
    {
        this.account = acc;
        this.authToken = token;
    }
    
    public void setProvider(ContentProviderClient client)
    {
        this.provider = client;
    }

    public void setProgressListener(OnProgressUpdate listener)
    {
        this.progressListener = listener;
    }

    private static class DownloadingNow extends Exception
    {
        private static final long serialVersionUID = 1L;

        public DownloadingNow()
        {
            super("downloading now");
        }
    }

    public void reset(Voicemail vm)
    {
        voicemail = vm;
    }

    @Override
    public void run()
    {
        if(voicemail.getId() <= 0)
        {
            Log.i(TAG, "need to query for voicemail ID");
            if(provider != null)
            {
                try
                {
                    voicemail = VoicemailHelper.getVoicemailFromServerID(provider, voicemail);
                }
                catch(RemoteException e)
                {
                    Log.e(TAG, "remote exception looking for voicemail locally for download", e);
                    voicemail = null;
                }
            }
            else
            {
                voicemail = VoicemailHelper.getVoicemailFromServerID(resolver, voicemail);
            }
            if(voicemail == null)
            {
                Log.e(TAG, "unable to find voicemail locally: " + voicemail);
                return;
            }
        }
        
        if(!VoicemailHelper.shouldAttemptDownload(voicemail, true))
        {
            Log.i(TAG, "voicemail downloaded or downloading now: " + voicemail);
            return;
        }

        if(account == null)
        {
            account = findAccount(accountManager.getAccountsByType(Constants.ACCOUNT_TYPE), voicemail.getUserName());
            if(account == null)
            {
                Log.v(TAG, "account not found: " + voicemail.getUserName());
                return;
            }
        }
        long downloaded = -1;
        try
        {
            downloaded = download();
        }
        catch(DownloadingNow e)
        {
            Log.i(TAG, "currently downloading");
            return;
        }
        catch(OperationCanceledException e)
        {
            Log.e(TAG, "download canceled");
        }
        catch(IOException e)
        {
            Log.e(TAG, "IO exception", e);
            VoicemailNotifyReceiver.broadcastConnectionError(context, account.name, e);
        }
        catch(AuthorizationException e)
        {
            Log.e(TAG, "authorization exception getting input stream for download", e);
            VoicemailNotifyReceiver.broadcastConnectionError(context, account.name, e);
        }
        catch(AuthenticatorException e)
        {
            Log.e(TAG, "authenticator exception", e);
            VoicemailNotifyReceiver.broadcastConnectionError(context, account.name, e);
        }
        catch(RemoteException e)
        {
            Log.e(TAG, "remote exception getting write stream", e);
        }
        
        if(isInterrupted() && downloaded == -1)
        {
            voicemail.clearDownloaded();
        }
        else
        {
            voicemail.markDownloaded(context, downloaded, type);
        }
        
        ContentValues values = voicemail.getAudioStateValues();
        if(provider != null)
        {
            try
            {
                provider.update(voicemail.getUri(), values, null, null);
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "error updating voicemail download state", e);
            }
        }
        else
        {
            resolver.update(voicemail.getUri(), values, null, null);
        }
    }

    protected void progressUpdate(int percent)
    {
        if(progressListener != null)
        {
            progressListener.onProgressUpdate(percent);
        }
    }

    protected boolean isInterrupted()
    {
        return Thread.currentThread().isInterrupted();
    }
    
    private final WavConversion.OnProgressUpdate mListener = new WavConversion.OnProgressUpdate()
    {
        @Override
        public void onProgressUpdate(int percent)
        {
            progressUpdate(percent);
        }

        @Override
        public void onTypeKnown(Type t)
        {
            type = t;
            Log.v(TAG, "determined type: " + type.getMIME());
        }
    };
    
    private long download(InputStream in, OutputStream out, long downloadSize) throws IOException
    {
        return WavConversion.copyToLinear(in, out, downloadSize, mListener);
    }
    
    private long download() throws AuthenticatorException, AuthorizationException, OperationCanceledException,
         IOException, RemoteException, DownloadingNow
    {
        if(authToken == null)
        {
            authToken = accountManager.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
            if(authToken == null)
            {
                Log.e(TAG, "unable to authenticate to download voicemail");
                throw new AuthenticatorException("auth token null");
            }
        }

        String hStr = accountManager.getUserData(account, Authenticator.HOST_DATA);
        if(hStr == null)
        {
            Log.e(TAG, "seem to authenticate but no host?");
            throw new AuthorizationException(Authenticator.HOST_DATA + " not set");
        }
        
        Log.i(TAG, "authorized for download " + hStr + " " + voicemail);
        Uri host = Uri.parse(hStr);
        String userName = account.name;
        
        HttpEntity entity = ClientUtilities.getVoicemailInput(host, userName, authToken, voicemail.getVoicemailId());
        if(entity == null)
        {
            Log.e(TAG, "unable to get voicemail HttpEntity for " + host + " - " + userName + " - " + voicemail.getVoicemailId());
            return -1;
        }
        
        if(isInterrupted())
        {
            return -1;
        }
        
        long downloadSize = entity.getContentLength();
        InputStream in = entity.getContent();
        
        Log.i(TAG, "download bytes: " + downloadSize);
        
        OutputStream out = null;
        long downloaded = -1;
        boolean didOpen = false;
        
        try
        {
            ParcelFileDescriptor pfd = null;
            if(provider != null)
            {
                pfd = VoicemailHelper.getDownloadStream(provider, voicemail);
            }
            else
            {
                pfd = VoicemailHelper.getDownloadStream(resolver, voicemail);
            }
            
            if(pfd == null)
            {
                throw new DownloadingNow();
            }
            didOpen = true;
            
            out = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
            downloaded = download(in, out, downloadSize);
        }
        finally
        {
            if(in != null)
            {
                try
                {
                    in.close();
                }
                catch(IOException e)
                {
                    Log.e(TAG, "error closing input stream", e);
                }
            }
            if(out != null)
            {
                try
                {
                    out.close();
                }
                catch(IOException e)
                {
                    Log.e(TAG, "error closing output stream", e);
                }
            }
        }
        Log.i(TAG, "downloading result: " + downloaded);
        
        if(downloaded < 0 && didOpen && voicemail != null)
        {
            Log.v(TAG, "need to clean up");
            VoicemailHelper.setDownloadCancelled(resolver, voicemail.getId());
        }
        
        return downloaded;
    }
    
    private Account findAccount(Account[] accounts, String userName)
    {
        if(accounts == null || userName == null)
        {
            Log.e(TAG, "account or user name not set");
            return null;
        }
        for (Account a : accounts)
        {
            if(TextUtils.equals(a.name, userName))
            {
                Log.i(TAG, "found account " + a.name);
                return a;
            }
        }
        Log.e(TAG, "couldn't find account " + userName);
        return null;
    }

    @Override
    public int compareTo(DownloadRunnable dr)
    {
        int id1 = 0, id2 = 0;
        int dt1 = 0, dt2 = 0;
        if(voicemail != null)
        {
            id1 = voicemail.getId();
            dt1 = (int)(voicemail.getDateCreatedMS() / 1000);
        }
        if(dr.voicemail != null)
        {
            id2 = dr.voicemail.getId();
            dt2 = (int)(dr.voicemail.getDateCreatedMS() / 1000);
        }
        return dt1 != dt2 ? dt1 - dt2 : id1 - id2;
    }

}
