package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ViewVoicemailActivity extends Activity
{
    private static final String TAG = Constants.TAG + "ViewVoicemail";

    private TextView mLeftBy = null;
    private TextView mDate = null;
    private TextView mTranscription = null;
    private MenuItem mDelete = null;

    private VoicemailPlayer mVoicemailPlayer = new VoicemailPlayer();
    private VoicemailContentObserver mContentObserver = null;
    private DownloadTask mDownloadTask = null;
    private MarkRead mReadTask = null;
    
    private int mVoicemailId;
    private Cursor mCursor;
    private Voicemail mVoicemail;
    private boolean vmUpdated;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicemail_view);

        mVoicemail = null;
        vmUpdated = false;

        Bundle extras = getIntent().getExtras();
        mVoicemailId = extras.getInt(Constants.EXTRA_ID);

        mLeftBy = (TextView)findViewById(R.id.detailLeftBy);
        mDate = (TextView)findViewById(R.id.detailDate);
        mTranscription = (TextView)findViewById(R.id.detailTranscription);
        
        AudioController audioController = new AudioController();
        audioController.initializeController(findViewById(R.id.audioController));
        mVoicemailPlayer.setAudioController(audioController);
        
        mCursor = mVoicemailId == 0 ? null : VoicemailHelper.getVoicemailDetailsCursor(getContentResolver(), mVoicemailId);

        if(mCursor == null || !mCursor.moveToFirst())
        {
            Log.i(TAG, "no voicemail found for " + mVoicemailId);
            if(mCursor != null)
            {
                mCursor.close();
                mCursor = null;
            }
        }
        else
        {
            startManagingCursor(mCursor);
            mContentObserver = new VoicemailContentObserver(new Handler());
            mCursor.registerContentObserver(mContentObserver);
        }

    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        updateView(false);
    }
    
    private class MarkRead extends AsyncTask<Voicemail, Integer, Integer>
    {
        protected Integer doInBackground(Voicemail... voicemails)
        {
            int i = 0;
            String userName = null;
            for(Voicemail v : voicemails)
            {
                if(v != null && v.getIsNew())
                {
                    VoicemailHelper.markVoicemailRead(getContentResolver(), v, true);
                    ++i;
                    userName = v.getUserName();
                }
            }
            if(userName != null)
            {
                SyncSchedule.syncUpdate(ViewVoicemailActivity.this, userName);
            }
            return i;
        }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_voicemail_view, menu);
        mDelete = menu.findItem(R.id.voicemail_view_delete);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        Log.v(TAG, "onMenuItemSelected()");
        switch(item.getItemId())
        {
            case R.id.voicemail_view_delete:
                if(mVoicemail != null)
                {
                    mVoicemailPlayer.stopPlayback();
                    VoicemailHelper.moveVoicemailToTrash(getContentResolver(), mVoicemailId);
                    vmUpdated = true;
                    SyncSchedule.syncUpdate(this, mVoicemail.getUserName());
                }                    
                setOkResult();
                finish();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }

    }

    @Override
    public void onDestroy()
    {
        if(mDownloadTask != null)
        {
            mDownloadTask.cancel(true);
        }
        mVoicemailPlayer.stopPlayback();
        super.onDestroy();
    }

    @Override
    public void onContentChanged()
    {
        super.onContentChanged();
        //if(mCursor != null)
        //{
        //    updateView(true);
        //}
    }
    
    private void updateView(boolean reQuery)
    {
        if(reQuery && mCursor != null)
        {
            Log.i(TAG, "requerying " + mVoicemailId);
            mCursor.unregisterContentObserver(mContentObserver);
            if(!mCursor.requery())
            {
                stopManagingCursor(mCursor);
                Log.e(TAG, "requery failed");
                mCursor = null;
            }
            else
            {
                mCursor.registerContentObserver(mContentObserver);
            }
        }
        
        if(mCursor == null || !mCursor.moveToFirst())
        {
            Log.i(TAG, "update view - no voicemail found for " + mVoicemailId);
            if(mCursor != null)
            {
                mCursor.unregisterContentObserver(mContentObserver);
                stopManagingCursor(mCursor);
                mCursor.close();
                mCursor = null;
            }
            mVoicemailPlayer.stopPlayback();
            mLeftBy.setText("");
            mDate.setText("");
            
            mTranscription.setText(R.string.voicemail_not_found);
            
            if(mDelete != null)
            {
                mDelete.setEnabled(false);
            }
            
            if(mDownloadTask != null)
            {
                mDownloadTask.cancel(true);
                mDownloadTask = null;
            }
        }
        else
        {
            mVoicemail = VoicemailHelper.getVoicemailDetailsFromCursor(mCursor);
            
            Log.v(TAG, "update view " + mVoicemail);
            
            if(mVoicemail.getLeftBy() == null)
            {
                mLeftBy.setText(R.string.leftByUnknown);
            }
            else
            {
                mLeftBy.setText(mVoicemail.getLeftBy());
            }
            if(mVoicemail.getTranscription() == null)
            {
                mTranscription.setText(R.string.transcriptionUnknown);
            }
            else
            {
                mTranscription.setText(mVoicemail.getTranscription());
            }
            mDate.setText(mVoicemail.getDateCreatedString(getString(R.string.dateCreatedUnknown)));
            
            if(mVoicemail.getIsNew() && mReadTask == null)
            {
                mReadTask = new MarkRead();
                mReadTask.execute(mVoicemail);
            }

            if(mVoicemail.isDownloading())
            {
                if(mDownloadTask == null)
                {
                    mVoicemailPlayer.getController().setEnabled(false);
                }
            }
            else if(mVoicemail.isDownloadError())
            {
                Toast.makeText(ViewVoicemailActivity.this, R.string.toast_download_error, Toast.LENGTH_SHORT).show();
                mVoicemailPlayer.getController().setEnabled(false);
            }
            else if(mVoicemail.isDownloaded())
            {
                mVoicemailPlayer.getController().setEnabled(true);
                if(!mVoicemailPlayer.isAudioSet())
                {
                    mVoicemailPlayer.setAudioURI(this, mVoicemail.getUri());
                }
            }
            else if(mDownloadTask == null)
            {
                if(mReadTask != null)
                {
                    try
                    {
                        mReadTask.get(0, TimeUnit.MILLISECONDS);
                        mReadTask = null;
                    }
                    catch(TimeoutException e)
                    {
                        Log.v(TAG, "not done marking download read");
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, "error checking if mark read is done", e);
                        mReadTask.cancel(false);
                        mReadTask = null;
                    }
                }
                if(mReadTask == null)
                {
                    mDownloadTask = new DownloadTask(this, mVoicemailPlayer, mVoicemail);
                    mDownloadTask.execute(); // TODO: multiple connection issue if still in syncing?
                }
            }
        }
    }
    
    private class VoicemailContentObserver extends ContentObserver
    {

        public VoicemailContentObserver(Handler handler)
        {
            super(handler);
        }
        
        @Override
        public boolean deliverSelfNotifications()
        {
            return true;
        }

        @Override
        public void onChange(boolean selfChange)
        {
            updateView(true);
        }
    }
    
    private void setOkResult()
    {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_ID, mVoicemailId);
        bundle.putBoolean(Constants.EXTRA_VOICEMAIL_UPDATED, vmUpdated);
        
        Intent intent = new Intent();
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
    }
    
}
