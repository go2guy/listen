package com.interact.listen.android.voicemail;

import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.interact.listen.android.voicemail.provider.VoicemailHelper;
import com.interact.listen.android.voicemail.provider.Voicemails;
import com.interact.listen.android.voicemail.sync.SyncSchedule;
import com.interact.listen.android.voicemail.widget.ContactBadge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ListVoicemailActivity extends ListActivity
{
    private static final String TAG = Constants.TAG + "ListVoicemails";

    private static final int VIEWED_DETAILS = 2;
    
    private Cursor mCursor = null;
    private ListVoicemailViewBinder mViewBinder = null;
    private ListVoicemailCursorAdapter mAdapter = null;
    private SyncStatusPoll mSyncStatusPoll = null;
    
    private Set<Integer> hasNotifiedIds = new TreeSet<Integer>();
    
    private static final String[] VOICEMAIL_INFO_COLUMNS =
        new String[]{Voicemails.LEFT_BY, Voicemails.LEFT_BY_NAME, Voicemails.DATE_CREATED, Voicemails.TRANSCRIPT};
    
    private static final int[] VOICEMAIL_INFO_VIEWS =
        new int[]{R.id.list_badge, R.id.list_leftby, R.id.list_date, R.id.list_transcription};
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        Log.v(TAG, "create list voicemail activity");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminate(true);

        setContentView(R.layout.voicemail_list);
        
        mCursor = VoicemailHelper.getVoicemailListInfoCursor(getContentResolver());
        startManagingCursor(mCursor);

        mViewBinder = new ListVoicemailViewBinder(this);

        mAdapter = new ListVoicemailCursorAdapter(this, R.layout.voicemail_list_item, mCursor, VOICEMAIL_INFO_COLUMNS, VOICEMAIL_INFO_VIEWS);
        mAdapter.setViewBinder(mViewBinder);
        
        setListAdapter(mAdapter);

        mAdapter.registerDataSetObserver(mDataSetObserver);
        
        ApplicationSettings.registerListener(this, mPrefListener);
        
        registerForContextMenu(getListView());

        updateView();
        
        AccountManager am =  AccountManager.get(this);
        if(am.getAccountsByType(Constants.ACCOUNT_TYPE).length == 0)
        {
            //Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            //intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{VoicemailProvider.AUTHORITY});
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //startActivity(intent);
            
            am.addAccount(Constants.ACCOUNT_TYPE, null, null, null, this, null, null);
        }
    }

    private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener()
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            if(mViewBinder != null && TextUtils.equals(key, ApplicationSettings.SYNC_EXTERNAL))
            {
                mViewBinder.clearCache();
            }
        }
    };

    private DataSetObserver mDataSetObserver = new DataSetObserver()
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
    };
    
    @Override
    public void onStart()
    {
        super.onStart();

    }
    
    @Override
    protected void onPause()
    {
        Log.v(TAG, "pause list voicemail activity");

        if(mSyncStatusPoll != null)
        {
            mSyncStatusPoll.cancel(true);
        }
        
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "resume list voicemail activity");

        if(mViewBinder != null)
        {
            mViewBinder.clearCache();
        }
        hasNotifiedIds.clear();

        super.onResume();

        NotificationHelper.clearNotificationBar(this);

        if(mSyncStatusPoll == null || mSyncStatusPoll.isCancelled())
        {
            mSyncStatusPoll = new SyncStatusPoll(this);
            mSyncStatusPoll.execute((Void[])null);
        }
    }
    
    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "destroy list voicemail activity");
        hasNotifiedIds.clear();
        
        if(mPrefListener != null)
        {
            ApplicationSettings.unregisterListener(this, mPrefListener);
        }
        
        if(mAdapter != null)
        {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mAdapter.onDestroy();
        }
        if(mViewBinder != null)
        {
            mViewBinder.onDestroy();
        }
        
        mAdapter = null;
        mSyncStatusPoll = null;
        mCursor = null;
        mDataSetObserver = null;
        mPrefListener = null;
        mAdapter = null;
        
        super.onDestroy();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
        
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        Cursor cursor = (Cursor)mAdapter.getItem(info.position);
        if(cursor == null)
        {
            Log.e(TAG, "no item at position " + info.position);
            return;
        }

        MenuItem turnOff = null;
        if(cursor.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_IS_NEW) != 0)
        {
            turnOff = menu.findItem(R.id.voicemail_mark_unread);
        }
        else
        {
            turnOff = menu.findItem(R.id.voicemail_mark_read);
        }
        if(turnOff != null)
        {
            turnOff.setEnabled(false);
            turnOff.setVisible(false);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        Cursor cursor = (Cursor)mAdapter.getItem(info.position);
        if(cursor == null)
        {
            Log.e(TAG, "no item at position " + info.position);
            return super.onContextItemSelected(item);
        }

        int id = cursor.getInt(0);

        boolean read = false;
        
        switch(item.getItemId())
        {
            case R.id.voicemail_context_view:
                triggerView(id);
                return true;
            case R.id.voicemail_context_call:
                NotificationHelper.dial(this, cursor.getString(1));
                return true;
            case R.id.voicemail_context_delete:
                NotificationHelper.alertDelete(this, id, null);
                return true;
            case R.id.voicemail_mark_read:
                read = true;
                break;
            case R.id.voicemail_mark_unread:
                read = false;
                break;
            default:
                return super.onContextItemSelected(item);
        }
        
        Intent intent = new Intent(Constants.ACTION_MARK_READ);
        intent.putExtra(Constants.EXTRA_ID, id);
        intent.putExtra(Constants.EXTRA_IS_READ, read);
        startService(intent);

        return true;
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
                startActivity(i);
                return true;
            case R.id.voicemail_list_refresh:
                SyncSchedule.syncRegular(this, null, true);
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
        
        triggerView(cursor.getInt(0));
    }

    private void triggerView(int id)
    {
        Intent intent = new Intent(this, ViewVoicemailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.EXTRA_ID, id);

        //startActivityForResult(intent, VIEWED_DETAILS);
        startActivity(intent);
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
            int numNew = 0;
            int total = 0;
            int notNotified = 0;
            while(c.moveToNext())
            {
                if(c.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_IS_NEW) != 0)
                {
                    numNew++;
                }
                if(c.getInt(VoicemailHelper.VOICEMAIL_LIST_PROJECT_NOTIFIED) == 0)
                {
                    Integer id = c.getInt(0);
                    if(hasNotifiedIds.add(id))
                    {
                        ++notNotified;
                    }
                }
                total++;
            }
            c.moveToPosition(-1);
            String text = getString(R.string.list_view_label_with_info, numNew, total);
            inboxStatus.setText(text);

            if(notNotified > 0)
            {
                Log.i(TAG, "starting mark voicemail service for un-notified voicemails: " + notNotified);
                Intent intent = new Intent(this, MarkVoicemailsService.class);
                intent.setAction(Constants.ACTION_MARK_NOTIFIED);
                startService(intent);
            }
        }
        else
        {
            inboxStatus.setText(R.string.list_view_label);
        }
        
    }

    private static final class BadgeHandler implements ContactBadge.OnComplete
    {
        private Set<TextView> views;
        private Set<ContactBadge> badges;
        private ContactBadge master;
        private ContactBadge.Data info;
        
        public BadgeHandler()
        {
            views = new HashSet<TextView>();
            badges = new HashSet<ContactBadge>();
            master = null;
            info = null;
        }
        
        public boolean isMaster(ContactBadge badge)
        {
            return master == badge;
        }

        public String addView(String leftBy, TextView view, String leftByName)
        {
            if(info != null)
            {
                if(!TextUtils.isEmpty(info.getContactName()))
                {
                    return info.getContactName();
                }
            }
            else
            {
                views.add(view);
            }
            return TextUtils.isEmpty(leftByName) ? leftBy : leftByName;
        }
        
        public void addBadge(String phoneNumber, ContactBadge badge)
        {
            if(info != null)
            {
                badge.assignFromInfo(info);
            }
            else if(master == null)
            {
                master = badge;
                badge.assignContactFromPhone(phoneNumber, false);
                badge.setOnCompleteListener(this);
            }
            else if(master != badge)
            {
                if(badges.add(badge))
                {
                    badge.assignContactFromPhone(phoneNumber, true);
                }
            }
        }
        
        @Override
        public void onComplete(ContactBadge.Data data)
        {
            info = data;
            if(!TextUtils.isEmpty(info.getContactName()))
            {
                for(TextView view : views)
                {
                    view.setText(info.getContactName());
                }
            }
            for(ContactBadge badge : badges)
            {
                badge.assignFromInfo(info);
            }
            views.clear();
            badges.clear();
            master = null;
        }

        public void remove(TextView view, ContactBadge badge)
        {
            views.remove(view);
            badges.remove(badge);
            if(master == badge)
            {
                master = null;
            }
        }
        
        public void clearInfo()
        {
            info = null;
        }
        
        public void clearAll()
        {
            views.clear();
            badges.clear();
            master = null;
            info = null;
        }
    }

    private static final class ListVoicemailCursorAdapter extends SimpleCursorAdapter
    {
        private Context mContext;
        private int[] mColors;
        
        public ListVoicemailCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to)
        {
            super(context, layout, c, from, to);
            mContext = context;
            
            mColors = new int[2];
            mColors[0] = context.getResources().getColor(R.color.list_item_background1);
            mColors[1] = context.getResources().getColor(R.color.list_item_background2);

            if(mColors[0] == mColors[1])
            {
                mColors = null;
            }
        }

        public void onDestroy()
        {
            this.setViewBinder(null);
            mContext = null;
        }
        
        @Override
        public View getView(int position, View convertView, android.view.ViewGroup parent)
        {
            ListVoicemailViewBinder binder = (ListVoicemailViewBinder)getViewBinder();
            Cursor cursor = getCursor();
            if (cursor == null || binder == null || mContext == null)
            {
                throw new IllegalStateException("cursor adapter looks to be destroyed");
            }
            if (!cursor.moveToPosition(position))
            {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            
            View v;
            if (binder != null && binder.isReUsable(cursor, convertView))
            {
                v = convertView;
            }
            else
            {
                v = newView(mContext, cursor, parent);
            }

            bindView(v, mContext, cursor);

            if(mColors != null)
            {
                v.setBackgroundColor(mColors[position % mColors.length]);
            }
            
            return v;
        }

    }
    
    private static final class ListVoicemailViewBinder implements SimpleCursorAdapter.ViewBinder
    {
        private Context context;
        private Map<String, BadgeHandler> leftByNames;
        
        public ListVoicemailViewBinder(Context c)
        {
            context = c;
            leftByNames = new TreeMap<String, BadgeHandler>();
        }

        public void clearCache()
        {
            for(Map.Entry<String, BadgeHandler> entry : leftByNames.entrySet())
            {
                entry.getValue().clearInfo();
            }
        }

        public void onDestroy()
        {
            for(Map.Entry<String, BadgeHandler> entry : leftByNames.entrySet())
            {
                entry.getValue().clearAll();
            }
            context = null;
            leftByNames.clear();
        }

        private String getCursorString(Cursor c, int cIdx)
        {
            return c.isNull(cIdx) ? "" : c.getString(cIdx);
        }
        
        public boolean isReUsable(Cursor cursor, View view)
        {
            if(view == null)
            {
                return false;
            }
            
            ContactBadge badge = (ContactBadge)view.findViewById(R.id.list_badge);
            TextView leftBy = (TextView)view.findViewById(R.id.list_leftby);

            if(badge == null || leftBy == null)
            {
                return true;
            }

            String text = getCursorString(cursor, 1); // 'left by' must be first in list after id

            for(Map.Entry<String, BadgeHandler> entry : leftByNames.entrySet())
            {
                if(entry.getValue().isMaster(badge))
                {
                    Log.v(TAG, "isReUsable - badge is part of master " + text + " - " + entry.getKey());
                    return text.equals(entry.getKey());
                }
                if(!text.equals(entry.getKey()))
                {
                    entry.getValue().remove(leftBy, badge);
                }
            }
            
            //Log.v(TAG, "isReUsable - reusable " + text + " - " + leftBy.getText());
            return true;
        }
        
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex)
        {
            String text = null;
            switch(view.getId())
            {
                case R.id.list_badge:
                    text = getCursorString(cursor, columnIndex);
                    ContactBadge badge = (ContactBadge)view;
                    if(text.length() > 0)
                    {
                        BadgeHandler handler = leftByNames.get(text);
                        if(handler == null)
                        {
                            handler = new BadgeHandler();
                            leftByNames.put(text, handler);
                        }
                        String number = NotificationHelper.getDialString(context, text, false);
                        handler.addBadge(number, badge);
                    }
                    else
                    {
                        badge.clearInfo();
                    }
                    return true;
                case R.id.list_leftby:
                {
                    String leftByName = getCursorString(cursor, columnIndex);
                    String leftByNumber = getCursorString(cursor, 1); // 'left by' must be column 1

                    if(!TextUtils.isEmpty(leftByNumber))
                    {
                        BadgeHandler handler = leftByNames.get(leftByNumber);
                        if(handler == null)
                        {
                            handler = new BadgeHandler();
                            leftByNames.put(leftByNumber, handler);
                        }
                        text = handler.addView(leftByNumber, (TextView)view, leftByName);
                    }
                    else if(!TextUtils.isEmpty(leftByName))
                    {
                        text = leftByName;
                    }
                    else
                    {
                        text = context.getString(R.string.leftByUnknown);
                    }
                    break;
                }
                case R.id.list_transcription:
                    text = getCursorString(cursor, columnIndex);
                    if(TextUtils.isEmpty(text))
                    {
                        text = context.getString(R.string.transcriptionUnknown);
                    }
                    else
                    {
                        text = getTruncatedTranscription(text);
                    }
                    break;
                case R.id.list_date:
                    if(cursor.isNull(columnIndex))
                    {
                        text = context.getString(R.string.dateCreatedUnknown);
                    }
                    else
                    {
                        text = Voicemail.getDateCreatedFromMS(context, cursor.getLong(columnIndex), true);
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

        private static final int TRANS_LENGTH = 70;
        
        private String getTruncatedTranscription(String fullTranscription)
        {
            StringBuilder returnString = new StringBuilder("");
            if(!TextUtils.isEmpty(fullTranscription))
            {
                // add the transcription to what we will return
                returnString.append(fullTranscription);
                if(fullTranscription.length() > TRANS_LENGTH)
                {
                    // add ... and only show the first 45 characters
                    return returnString.insert(TRANS_LENGTH - 3, "...").substring(0, TRANS_LENGTH);
                }
            }

            return returnString.toString();
        }

    }
}
