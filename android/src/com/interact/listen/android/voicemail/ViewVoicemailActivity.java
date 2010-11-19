package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;

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
    
    private int mVoicemailId;
    private Cursor mCursor;
    private Voicemail mVoicemail;
    private boolean vmUpdated;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "creating view activity");

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

        if(mVoicemailId > 0)
        {
            Intent intent = new Intent(Constants.ACTION_MARK_READ);
            intent.putExtra(Constants.EXTRA_ID, mVoicemailId);
            intent.putExtra(Constants.EXTRA_IS_READ, true);
            startService(intent);
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "pausing view activity");
        mVoicemailPlayer.triggerPause();
        if (mVoicemailPlayer.getController() != null)
        {
            mVoicemailPlayer.getController().onFocus(false);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "resuming view activity");
        if (mVoicemailPlayer.getController() != null)
        {
            mVoicemailPlayer.getController().onFocus(true);
        }
        updateView(false);
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
                    mVoicemailPlayer.triggerPause();
                    NotificationHelper.alertDelete(this, mVoicemailId, new NotificationHelper.OnConfirm()
                    {
                        @Override
                        public void onConfirmed(Voicemail voicemail)
                        {
                            vmUpdated = true;
                            setOkResult();
                            finish();
                        }
                    });
                }
                return true;
            case R.id.voicemail_view_inbox:
                Intent intent = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
            
            if(mDownloadTask == null)
            {
                if(mVoicemail.isDownloaded())
                {
                    if(!mVoicemailPlayer.isAudioSet())
                    {
                        mVoicemailPlayer.setAudioURI(this, mVoicemail.getUri());
                    }
                }
                else if(mDownloadTask == null)
                {
                    mDownloadTask = new DownloadTask(this, mVoicemailPlayer, mVoicemail);
                    mDownloadTask.execute();
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
