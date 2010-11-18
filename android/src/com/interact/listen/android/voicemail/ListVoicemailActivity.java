package com.interact.listen.android.voicemail;

import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.VoicemailProvider;
import com.interact.listen.android.voicemail.provider.Voicemails;
import com.interact.listen.android.voicemail.sync.SyncSchedule;

public class ListVoicemailActivity extends ListActivity
{
    private static final String TAG = Constants.TAG + "ListVoicemails";

    private static final int EDITED_SETTINGS = 1;
    private static final int VIEWED_DETAILS  = 2;
    
    private Cursor mCursor;
    private ListVoicemailViewBinder mViewBinder;
    private SimpleCursorAdapter mAdapter;
    
    private static final String[] VOICEMAIL_INFO_COLUMNS =
        new String[]{Voicemails.LEFT_BY, Voicemails.DATE_CREATED, Voicemails.TRANSCRIPT};
    
    private static final int[] VOICEMAIL_INFO_VIEWS =
        new int[]{R.id.leftBy, R.id.date, R.id.transcription};
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        Log.v(TAG, "create list voicemail activity");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.voicemail_list);
        
        mCursor = VoicemailHelper.getVoicemailListInfoCursor(getContentResolver());
        startManagingCursor(mCursor);

        mViewBinder = new ListVoicemailViewBinder(this);

        mAdapter = new SimpleCursorAdapter(this, R.layout.voicemail_list_item, mCursor, VOICEMAIL_INFO_COLUMNS, VOICEMAIL_INFO_VIEWS);
        mAdapter.setViewBinder(mViewBinder);
        
        setListAdapter(mAdapter);

        mAdapter.registerDataSetObserver(new DataSetObserver()
        {
            @Override
            public void onChanged()
            {
                updateView();
            }
            @Override
            public void onInvalidated()
            {
                updateView();
            }
        });
        
        updateView();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        AccountManager am =  AccountManager.get(this);
        if(am.getAccountsByType(Constants.ACCOUNT_TYPE).length == 0)
        {
            Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{VoicemailProvider.AUTHORITY});
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            
            //am.addAccount(Constants.ACCOUNT_TYPE, null, null, null, this, null, null);
        }

    }
    
    @Override
    protected void onPause()
    {
        Log.v(TAG, "pause list voicemail activity");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "resume list voicemail activity");
        super.onResume();

        NotificationHelper.clearNotificationBar(this);
        
        SyncSchedule.syncFull(this, false);
    }

    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "destroy list voicemail activity");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_voicemail_list, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        Log.v(TAG, "onMenuItemSelected()");
        switch(item.getItemId())
        {
            case R.id.voicemail_list_settings:
                Intent i = new Intent(this, ApplicationSettings.class);
                startActivityForResult(i, EDITED_SETTINGS);
                return true;
            case R.id.voicemail_list_refresh:
                SyncSchedule.syncFull(this, true);
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }

    }
    
    @Override
    protected void onListItemClick(ListView l, View view, int position, long id)
    {
        Log.v(TAG, "voicemail list view item selected " + position);
        super.onListItemClick(l, view, position, id);

        Cursor cursor = (Cursor)mAdapter.getItem(position);
        if(cursor == null)
        {
            Log.e(TAG, "no item at position " + position);
            return;
        }
        
        Intent intent = new Intent(this, ViewVoicemailActivity.class);
        intent.putExtra(Constants.EXTRA_ID, cursor.getInt(0));

        startActivityForResult(intent, VIEWED_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode)
        {
            case VIEWED_DETAILS:
            {
                if(intent == null || intent.getExtras() == null)
                {
                    Log.i(TAG, "on activity result from details viewed has null intent or extras");
                }
                else
                {
                    boolean updated = intent.getBooleanExtra(Constants.EXTRA_VOICEMAIL_UPDATED, false);
                    if(updated)
                    {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                break;
            }
            default:
                break;
        }
    }
    
    @Override
    public void onContentChanged()
    {
        super.onContentChanged();
        updateView();
    }

    private void updateView()
    {
        if(mAdapter == null)
        {
            return;
        }
        
        TextView inboxStatus = (TextView)findViewById(R.id.list_view_label);
        if(mAdapter.getCount() > 0 && mAdapter.getCursor() != null)
        {
            Cursor c = mAdapter.getCursor();
            c.moveToPosition(-1);
            int numNew = 0, notNotified = 0;
            int total = 0;
            while(c.moveToNext())
            {
                if(c.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_IS_NEW) != 0)
                {
                    numNew++;
                }
                if(c.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_NOTIFIED) == 0)
                {
                    notNotified++;
                }
                total++;
            }
            c.moveToPosition(-1);
            String text = getString(R.string.list_view_label_with_info, numNew, total);
            inboxStatus.setText(text);

            if(notNotified > 0)
            {
                Log.i(TAG, "starting mark voicemail service for un-notified voicemails: " + notNotified);
                // just go through all of them at the sqlite level, probably more efficient anyway
                Intent intent = new Intent(Constants.ACTION_MARK_NOTIFIED);
                startService(intent);
            }
        }
        else
        {
            inboxStatus.setText(R.string.list_view_label);
        }
        
    }
    
    private class ListVoicemailViewBinder implements SimpleCursorAdapter.ViewBinder
    {
        private Context context;
        
        public ListVoicemailViewBinder(Context c)
        {
            context = c;
        }
        
        private String getCursorString(Cursor c, int cIdx, int defId)
        {
            return c.isNull(cIdx) ? context.getString(defId) : c.getString(cIdx);
        }
        
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex)
        {
            String text = null;
            switch(view.getId())
            {
                case R.id.leftBy:
                    text = getCursorString(cursor, columnIndex, R.string.leftByUnknown);
                    break;
                case R.id.transcription:
                    text = getCursorString(cursor, columnIndex, R.string.transcriptionUnknown);
                    text = getTruncatedTranscription(text);
                    break;
                case R.id.date:
                    if(cursor.isNull(columnIndex))
                    {
                        text = context.getString(R.string.dateCreatedUnknown);
                    }
                    else
                    {
                        text = Voicemail.getDateCreatedFromString(cursor.getLong(columnIndex));
                    }
                    break;
                default:
                    return false;
            }
            
            if(text == null)
            {
                return false;
            }

            TextView tv = (TextView)view;
            tv.setText(text);
            
            if(cursor.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_IS_NEW) != 0)
            {
                tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            else
            {
                tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            return true;
        }

        private String getTruncatedTranscription(String fullTranscription)
        {
            StringBuilder returnString = new StringBuilder("");
            if(!TextUtils.isEmpty(fullTranscription))
            {
                // add the transcription to what we will return
                returnString.append(fullTranscription);
                if(fullTranscription.length() > 45)
                {
                    // Add ... and only show the first 45 characters
                    return returnString.insert(42, "...").substring(0, 45);
                }
            }

            return returnString.toString();
        }

    }
}
