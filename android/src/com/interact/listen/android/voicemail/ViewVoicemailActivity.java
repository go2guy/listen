package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.Voicemails;
import com.interact.listen.android.voicemail.widget.ContactBadge;
import com.interact.listen.android.voicemail.widget.ContactBadge.Data;

public class ViewVoicemailActivity extends Activity
{
    private static final String TAG = Constants.TAG + "ViewVoicemail";

    private TextView mName = null;
    private TextView mLeftBy = null;
    private TextView mDate = null;
    private TextView mTranscription = null;
    private MenuItem mDelete = null;
    private MenuItem mCall = null;
    private ContactBadge.Data mBadge = null;
    
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

        mName = (TextView)findViewById(R.id.voicemail_view_name);
        mLeftBy = (TextView)findViewById(R.id.voicemail_view_leftby);
        mDate = (TextView)findViewById(R.id.voicemail_view_received);
        mTranscription = (TextView)findViewById(R.id.voicemail_view_transcript);
        
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
    protected void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
        if(mVoicemailPlayer != null)
        {
            mVoicemailPlayer.saveState(bundle);
        }
    }
    
    @Override
    protected void onRestoreInstanceState (Bundle bundle)
    {
        if(mVoicemailPlayer != null)
        {
            mVoicemailPlayer.restoreState(bundle);
        }
    }
    
    @Override
    protected void onStart()
    {
        super.onStart();
        
        if(mVoicemailId > 0 && mCursor != null && mCursor.getPosition() == 0 &&
            mCursor.getInt(mCursor.getColumnIndex(Voicemails.IS_NEW)) != 0)
        {
            Intent intent = new Intent(Constants.ACTION_MARK_READ);
            intent.putExtra(Constants.EXTRA_ID, mVoicemailId);
            intent.putExtra(Constants.EXTRA_IS_READ, true);
            startService(intent);
        }
    }
    
    @Override
    protected void onPause()
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
    protected void onResume()
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
        mCall = menu.findItem(R.id.voicemail_view_call);
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
                Intent inbox = new Intent(Constants.ACTION_LISTALL_VOICEMAIL);
                inbox.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(inbox);
                return true;
            case R.id.voicemail_view_call:
                if(mVoicemail != null)
                {
                    NotificationHelper.dial(this, mVoicemail.getLeftBy());
                }
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
            mName.setText(R.string.leftByUnknown);
            mLeftBy.setText("");
            mDate.setText("");
            
            ContactBadge badge = (ContactBadge)findViewById(R.id.voicemail_view_badge);
            if(badge != null)
            {
                badge.clearInfo();
            }
            
            mTranscription.setText(R.string.voicemail_not_found);
            
            if(mDelete != null)
            {
                mDelete.setEnabled(false);
            }
            if(mCall != null)
            {
                mCall.setEnabled(false);
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
                mLeftBy.setText("");
                mName.setText(R.string.leftByUnknown);
            }
            else
            {
                if(mBadge != null && !TextUtils.isEmpty(mBadge.getContactName()))
                {
                    mName.setText(mBadge.getContactName());
                }
                else
                {
                    String leftBy = NotificationHelper.getDialString(this, mVoicemail.getLeftBy(), false);
                    mName.setText(PhoneNumberUtils.formatNumber(leftBy));
                }
                mLeftBy.setText(PhoneNumberUtils.formatNumber(mVoicemail.getLeftBy()));
            }
            if(TextUtils.isEmpty(mVoicemail.getTranscription()))
            {
                mTranscription.setText(R.string.transcriptionUnknown);
            }
            else
            {
                mTranscription.setText(mVoicemail.getTranscription());
            }
            String dateString = mVoicemail.getDateCreatedString(this, false, getString(R.string.dateCreatedUnknown));
            String durString = mVoicemail.getDurationString();
            mDate.setText(getString(R.string.received_on_label, dateString, durString));
            
            ContactBadge badge = (ContactBadge)findViewById(R.id.voicemail_view_badge);
            if(badge != null)
            {
                if(mBadge == null)
                {
                    String leftBy = NotificationHelper.getDialString(this, mVoicemail.getLeftBy(), false);
                    badge.setOnCompleteListener(new ContactBadge.OnComplete()
                    {
                        @Override
                        public void onComplete(Data info)
                        {
                            mBadge = info;
                            if(mName != null && !TextUtils.isEmpty(mBadge.getContactName()))
                            {
                                mName.setText(mBadge.getContactName());
                            }
                        }
                    });
                    badge.assignContactFromPhone(leftBy, false);
                }
                else
                {
                    badge.assignFromInfo(mBadge);
                }
            }
            
            if(mDownloadTask == null)
            {
                if(mVoicemail.isDownloaded())
                {
                    Log.v(TAG, "audio downloaded");
                    if(!mVoicemailPlayer.isAudioSet())
                    {
                        mVoicemailPlayer.setAudioURI(this, mVoicemail.getUri());
                    }
                }
                else
                {
                    Log.v(TAG, "creating download task");
                    mDownloadTask = new DownloadTask(this, mVoicemailPlayer, mVoicemail);
                    mDownloadTask.execute((Void[])null);
                }
            }
            else if(!mVoicemail.isDownloaded())
            {
                Log.v(TAG, "download task: " + mDownloadTask.getStatus());
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
