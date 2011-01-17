package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.EntityIterator;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.interact.listen.android.voicemail.contact.ContactEntryAdapter;
import com.interact.listen.android.voicemail.contact.ContactEntryAdapter.ContactAction;
import com.interact.listen.android.voicemail.contact.ContactType;
import com.interact.listen.android.voicemail.contact.ListenContacts;
import com.interact.listen.android.voicemail.sync.Authority;

import java.util.ArrayList;

public class ViewContactActivity extends Activity implements View.OnCreateContextMenuListener,
    DialogInterface.OnClickListener, AdapterView.OnItemClickListener
{
    private static final String TAG = Constants.TAG + "ViewContact";

    private static final int DIALOG_HIDE_CONTACT = 1;

    private static final int TOKEN_ENTITIES = 0;

    private Uri mDataUri = null;
    
    private long mRawID = -1;
    private long mContactID = -1;
    private String mDisplayName = null;
    
    private ArrayList<ViewEntry> mPhoneEntries = new ArrayList<ViewEntry>();
    private ArrayList<ViewEntry> mEmailEntries = new ArrayList<ViewEntry>();
    private ArrayList<ArrayList<ViewEntry>> mSections = new ArrayList<ArrayList<ViewEntry>>();

    private QueryHandler mHandler;
    private ArrayList<Entity> mEntities = new ArrayList<Entity>();
    private boolean mHasEntities = false;

    private Cursor mCursor = null;

    private LayoutInflater mInflater;
    private ContentResolver mResolver;
    private ViewAdapter mAdapter;
    private View mEmptyView;
    private View mFooterView;
    private ListView mListView;

    private ContentObserver mObserver = new ContentObserver(new Handler())
    {
        @Override
        public boolean deliverSelfNotifications()
        {
            return true;
        }

        @Override
        public void onChange(boolean selfChange)
        {
            if(mCursor != null && !mCursor.isClosed())
            {
                startEntityQuery();
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contact_view);

        mResolver = getContentResolver();
        mHandler = new QueryHandler(mResolver);
        mListView = (ListView)findViewById(R.id.contact_view_data);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
        mEmptyView = findViewById(android.R.id.empty);
        
        mFooterView = mInflater.inflate(R.layout.contact_view_footer, null, false);

        mSections.add(mPhoneEntries);
        mSections.add(mEmailEntries);
        
        setFromIntent(getIntent());
    }
    
    private void setFromIntent(Intent intent)
    {
        if(mDataUri == null || !mDataUri.equals(intent.getData()))
        {
            mPhoneEntries.clear();
            mEmailEntries.clear();

            mDataUri = intent.getData();
    
            if(Authority.getByAuthority(mDataUri.getAuthority()) == null)
            {
                Log.e(TAG, "authority not as expected: " + mDataUri.getAuthority());
                finish();
                return;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        setFromIntent(intent);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        startEntityQuery();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        closeCursor();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        closeCursor();
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch(id)
        {
            case DIALOG_HIDE_CONTACT:
            {
                return new AlertDialog.Builder(this).setTitle(R.string.hideContactTitle)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setMessage(R.string.hideContactMessage)
                                                    .setPositiveButton(android.R.string.ok, this)
                                                    .setNegativeButton(R.string.generic_cancel, null).create();
            }
            default:
                return null;
        }
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        closeCursor();
        if(which == DIALOG_HIDE_CONTACT)
        {
            Log.v(TAG, "hide contact confirmed");
            // TODO: mark deleted in profile, delete raw contacts, then use profile marking to not add back during sync. un-comment onCreateOptionsMenu
            finish();
        }
    }

    private static final String[] PROFILE_PROJECTION = {RawContacts.Data.RAW_CONTACT_ID, RawContacts.Data.MIMETYPE, RawContacts.CONTACT_ID};
    
    private static Cursor setupRawContactCursor(ContentResolver resolver, Uri profileUri)
    {
        if(resolver == null || profileUri == null)
        {
            return null;
        }

        Uri qUri = profileUri;
        if(Authority.VOICEMAIL.get().equals(profileUri.getAuthority()))
        {
            Log.v(TAG, "coming from voicemail so display name");
            qUri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, profileUri.getLastPathSegment());
        }
        
        Cursor cursor = resolver.query(qUri, PROFILE_PROJECTION, null, null, null);

        if(!cursor.moveToFirst())
        {
            Log.e(TAG, "profile '" + qUri + "' not found");
            cursor.close();
            return null;
        }
        
        String mimeType = cursor.getString(1);
        if((qUri == profileUri && !ListenContacts.PROFILE_MIME.equals(mimeType)) ||
            (qUri != profileUri && !ListenContacts.LIVE_CONTENT_TYPE.equals(mimeType)))
        {
            Log.e(TAG, "unknown profile MIME '" + mimeType + "'");
            cursor.close();
            return null;
        }
        
        return cursor;
    }

    private synchronized void startEntityQuery()
    {
        closeCursor();

        mCursor = setupRawContactCursor(mResolver, mDataUri);
        if(mCursor == null)
        {
            Log.e(TAG, "cursor is null");
            Toast.makeText(this, R.string.invalidContactToast, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mRawID = mCursor.getLong(mCursor.getColumnIndex(RawContacts.Data.RAW_CONTACT_ID));
        mContactID = mCursor.getLong(mCursor.getColumnIndex(RawContacts.CONTACT_ID));
        Log.d(TAG, "found raw contact " + mRawID + "/" + mContactID);

        mCursor.registerContentObserver(mObserver);

        mHasEntities = false;

        mHandler.startQuery(TOKEN_ENTITIES, null, RawContactsEntity.CONTENT_URI, null, RawContacts.Entity._ID + "=?",
                            new String[] {Long.toString(mRawID)}, null);
    }

    private void closeCursor()
    {
        if(mCursor != null)
        {
            mCursor.unregisterContentObserver(mObserver);
            mCursor.close();
            mCursor = null;
        }
    }

    private void considerBindData()
    {
        if(mHasEntities)
        {
            bindData();
        }
    }

    private void bindData()
    {
        buildEntries();

        if(mAdapter == null)
        {
            if(mFooterView != null)
            {
                mListView.addFooterView(mFooterView, null, true);
            }

            mAdapter = new ViewAdapter(this, mSections);
            mListView.setAdapter(mAdapter);
        }
        else
        {
            mAdapter.setSections(mSections);
        }
        mListView.setEmptyView(mEmptyView);
        
        if(mDisplayName != null)
        {
            ((TextView)findViewById(R.id.contact_view_name)).setText(mDisplayName);
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_view, menu);
        return true;
    }
*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
        AdapterView.AdapterContextMenuInfo info;
        try
        {
            info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        }
        catch(ClassCastException e)
        {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        // This can be null sometimes, don't crash...
        if(info == null)
        {
            Log.e(TAG, "bad menuInfo");
            return;
        }

        ViewEntry entry = ContactEntryAdapter.getEntry(mSections, info.position);
        menu.setHeaderTitle(R.string.contactOptionsTitle);
        switch(entry.getAction())
        {
            case PHONE:
                menu.add(0, 0, 0, R.string.menu_call).setIntent(entry.primaryIntent);
                break;
            case PHONE_AND_SMS:
                menu.add(0, 0, 0, R.string.menu_call).setIntent(entry.primaryIntent);
                menu.add(0, 0, 0, R.string.menu_sms).setIntent(entry.secondaryIntent);
                break;
            case EMAIL:
                menu.add(0, 0, 0, R.string.menu_email).setIntent(entry.primaryIntent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_delete:
            {
                showDialog(DIALOG_HIDE_CONTACT);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch(keyCode)
        {
            case KeyEvent.KEYCODE_CALL:
            {
                //TODO: number = work extension number
                //NotificationHelper.dial(this, number);
                return super.onKeyDown(keyCode, event);
            }

            case KeyEvent.KEYCODE_DEL:
            {
                showDialog(DIALOG_HIDE_CONTACT);
                return true;
            }
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    public void onItemClick(@SuppressWarnings("rawtypes") AdapterView parent, View v, int position, long id)
    {
        if(v == mFooterView)
        {
            if(mContactID >= 0)
            {
                Uri lUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, mContactID);
                Intent intent = new Intent(Intent.ACTION_VIEW, lUri);
                startActivity(intent);
            }
            return;
        }
        
        ViewEntry entry = ViewAdapter.getEntry(mSections, position);
        if(entry != null)
        {
            Intent intent = entry.primaryIntent;
            if(intent != null)
            {
                try
                {
                    startActivity(intent);
                }
                catch(ActivityNotFoundException e)
                {
                    Log.e(TAG, "No activity found for intent: " + intent);
                    signalError();
                }
            }
            else
            {
                signalError();
            }
        }
        else
        {
            signalError();
        }
    }

    private void signalError()
    {
        Toast.makeText(this, R.string.noContactActionToast, Toast.LENGTH_SHORT).show();
    }

    private final void buildEntries()
    {
        final int numSections = mSections.size();
        for(int i = 0; i < numSections; i++)
        {
            mSections.get(i).clear();
        }

        for(Entity entity : mEntities)
        {
            final ContentValues entValues = entity.getEntityValues();
            final long rawContactId = entValues.getAsLong(RawContacts._ID);

            for(NamedContentValues subValue : entity.getSubValues())
            {
                final ContentValues entryValues = subValue.values;
                entryValues.put(Data.RAW_CONTACT_ID, rawContactId);

                final long dataId = entryValues.getAsLong(Data._ID);
                final String mimeType = entryValues.getAsString(Data.MIMETYPE);
                
                Log.v(TAG, "named content values for " + rawContactId + "/" + dataId + ": " + mimeType);

                if(mimeType == null || ListenContacts.PROFILE_MIME.equals(mimeType))
                {
                    continue;
                }

                if(StructuredName.CONTENT_ITEM_TYPE.equals(mimeType))
                {
                    mDisplayName = entryValues.getAsString(Data.DATA1);
                    continue;
                }
                
                final ContactType type = ListenContacts.getContactType(entryValues);
                final ContactAction action = ContactAction.getAction(mimeType, type);

                if(action == null)
                {
                    Log.e(TAG, "unable to determine action for " + mimeType + " type " + type);
                    continue;
                }
                if(type == null)
                {
                    Log.e(TAG, "unable to determine type for " + action);
                    continue;
                }
                
                final ViewEntry entry = ViewEntry.fromValues(this, type, action, rawContactId, dataId, entryValues);

                if(!TextUtils.isEmpty(entry.getData()))
                {
                    switch(action)
                    {
                        case EMAIL:
                            entry.primaryIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", entry.getData(), null));
                            mEmailEntries.add(entry);
                            break;
                        case PHONE_AND_SMS:
                            entry.secondaryIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", entry.getData(), null));
                            // fall through
                        case PHONE:
                            entry.primaryIntent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", entry.getData(), null));
                            mPhoneEntries.add(entry);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    private static final class ViewEntry extends ContactEntryAdapter.Entry
    {
        private Intent primaryIntent = null;
        private Intent secondaryIntent = null;

        private ViewEntry()
        {
            super();
        }

        public static ViewEntry fromValues(Context context, ContactType type, ContactAction action,
                                           long rawId, long dataId, ContentValues values)
        {
            final String lStr = action.getHeader(context, type.getViewLabel());
            final String dStr = ListenContacts.getContactValue(type, values, false);

            final ViewEntry entry = new ViewEntry();
            
            entry.fromValues(type, action, lStr, dStr, rawId, dataId);

            return entry;
        }
    }

    private static final class ViewCache
    {
        private TextView label;
        private TextView data;
        private TextView footer;
        private ImageView primaryIcon;
        private ImageView secondaryIcon;
        private View secondaryActionDivider;
        
        private ViewCache()
        {
        }
    }

    private final class ViewAdapter extends ContactEntryAdapter<ViewEntry> implements View.OnClickListener
    {

        ViewAdapter(Context context, ArrayList<ArrayList<ViewEntry>> sections)
        {
            super(context, sections);
        }

        public void onClick(View v)
        {
            Intent intent = (Intent)v.getTag();
            startActivity(intent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewEntry entry = getEntry(mSections, position);
            View v;

            ViewCache views;

            // Check to see if we can reuse convertView
            if(convertView != null)
            {
                v = convertView;
                views = (ViewCache)v.getTag();
            }
            else
            {
                v = mInflater.inflate(R.layout.contact_view_list_item, parent, false);

                views = new ViewCache();
                views.label = (TextView)v.findViewById(android.R.id.text1);
                views.data = (TextView)v.findViewById(android.R.id.text2);
                views.footer = (TextView)v.findViewById(R.id.footer);
                views.primaryIcon = (ImageView)v.findViewById(R.id.primary_action_button);
                views.secondaryIcon = (ImageView)v.findViewById(R.id.secondary_action_button);
                views.secondaryIcon.setOnClickListener(this);
                views.secondaryActionDivider = v.findViewById(R.id.divider);
                v.setTag(views);
            }

            bindView(v, entry);
            return v;
        }

        @Override
        protected View newView(int position, ViewGroup parent)
        {
            // getView() handles this
            throw new UnsupportedOperationException();
        }

        @Override
        protected void bindView(View view, ViewEntry entry)
        {
            final Resources resources = mContext.getResources();
            ViewCache views = (ViewCache)view.getTag();

            TextView label = views.label;
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setText(entry.getLabel());

            TextView data = views.data;
            if(data != null)
            {
                if(entry.getAction() == ContactAction.EMAIL)
                {
                    data.setText(entry.getData());
                }
                else
                {
                    data.setText(PhoneNumberUtils.formatNumber(entry.getData()));
                }
                data.setSingleLine(true);
                data.setEllipsize(TextUtils.TruncateAt.END);
            }

            // not doing a footer right now
            views.footer.setVisibility(View.GONE);

            // Set the action icon
            ImageView pAction = views.primaryIcon;
            pAction.setImageDrawable(resources.getDrawable(entry.getAction().getPrimaryIcon()));
            pAction.setVisibility(View.VISIBLE);

            ImageView sAction = views.secondaryIcon;
            if(entry.getAction().isSecondary())
            {
                sAction.setTag(entry.secondaryIntent);
                sAction.setImageDrawable(resources.getDrawable(entry.getAction().getSecondaryIcon()));
                sAction.setVisibility(View.VISIBLE);
                views.secondaryActionDivider.setVisibility(View.VISIBLE);
            }
            else
            {
                sAction.setVisibility(View.GONE);
                views.secondaryActionDivider.setVisibility(View.GONE);
            }
        }
    }

    private class QueryHandler extends AsyncQueryHandler
    {
        public QueryHandler(ContentResolver cr)
        {
            super(cr);
        }

        @Override
        protected void onQueryComplete(final int token, final Object cookie, final Cursor cursor)
        {
            final ArrayList<Entity> oldEntities = mEntities;
            (new AsyncTask<Void, Void, ArrayList<Entity>>()
            {
                @Override
                protected ArrayList<Entity> doInBackground(Void... params)
                {
                    ArrayList<Entity> newEntities = new ArrayList<Entity>(cursor.getCount());
                    EntityIterator iterator = RawContacts.newEntityIterator(cursor);
                    try
                    {
                        while(iterator.hasNext())
                        {
                            Entity entity = iterator.next();
                            newEntities.add(entity);
                        }
                    }
                    finally
                    {
                        iterator.close();
                    }
                    return newEntities;
                }

                @Override
                protected void onPostExecute(ArrayList<Entity> newEntities)
                {
                    if(newEntities == null)
                    {
                        return; // there was an error loading
                    }
                    synchronized(ViewContactActivity.this)
                    {
                        if(mEntities != oldEntities)
                        {
                            return; // last race
                        }
                        mEntities = newEntities;
                        mHasEntities = true;
                    }
                    considerBindData();
                }
            }).execute();
        }
    };
    
}
