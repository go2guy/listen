package com.interact.listen.android.voicemail;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.*;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListenVoicemail extends ListActivity
{
    private static final String TAG = ListenVoicemail.class.getName();

    private static final int VIEW_DETAILS = 0;
    private static final int EDIT_SETTINGS = 1;
    private static final int LISTEN_NOTIFICATION = 45;
    private static final int MENU_SETTINGS_ID = Menu.FIRST;

    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    private ArrayList<Voicemail> mVoicemails = new ArrayList<Voicemail>();
    private ListenVoicemailServiceBinder serviceBinder = new ListenVoicemailServiceBinder(this);

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.v(TAG, "onRecieve()");

            abortBroadcast();
            try
            {
                new VoicemailParser().onPostExecute(serviceBinder.getService().getVoicemails());
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Error retreiving voicemails from the service: " + e);
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        Log.v(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        VoicemailListAdapter adapter = new VoicemailListAdapter(new ArrayList<Voicemail>(0));
        setListAdapter(adapter);
    }

    @Override
    protected void onResume()
    {
        Log.v(TAG, "onResume()");        
        super.onResume();

        serviceBinder.bind(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new VoicemailParser().onPostExecute(serviceBinder.getService().getVoicemails());
                }
                catch(RemoteException e)
                {
                    Log.e(TAG, "Error retreiving voicemails from the service: " + e);
                }
            }
        });
        
        clearNotificationBar();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION_STRING);
        filter.setPriority(2);

        this.registerReceiver(this.receiver, filter);
    }

    @Override
    protected void onPause()
    {
        Log.v(TAG, "onPause()");
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }

    @Override
    protected void onDestroy()
    {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
        serviceBinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.v(TAG, "onCreateOptionsMenu()");
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SETTINGS_ID, 0, R.string.menu_settings);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        Log.v(TAG, "onMenuItemSelected()");
        switch(item.getItemId())
        {
            case MENU_SETTINGS_ID:
                Intent i = new Intent(this, ApplicationSettings.class);
                startActivityForResult(i, EDIT_SETTINGS);
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View view, int position, long id)
    {
        Log.v(TAG, "onListItemClick()");
        super.onListItemClick(l, view, position, id);
        VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
        Voicemail voicemail = (Voicemail)adapter.getItem(position);
        Intent intent = new Intent(this, VoicemailDetails.class);
        intent.putExtra("id", voicemail.getId());
        intent.putExtra("leftBy", voicemail.getLeftBy());
        intent.putExtra("date", voicemail.getDateCreated());
        intent.putExtra("transcription", voicemail.getTranscription());
        intent.putExtra("position", position);

        // mark voicemail as old
        List<Voicemail> voicemails = adapter.getData();
        voicemails.get(position).setIsNew(false);
        adapter.setData(voicemails);

        startActivityForResult(intent, VIEW_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        Log.v(TAG, "onActivityResult");
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode)
        {
            case VIEW_DETAILS:
            {
                // TODO basing logic here on the 'intent' value may cause weirdness in the future
                // if there are more actions that can be done on the details screen
                if(intent != null) // intent is not null, meaning they clicked "Delete"
                {
                    VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
                    List<Voicemail> voicemails = adapter.getData();

                    Bundle extras = intent.getExtras();
                    int position = extras.getInt("position");
                    Log.v(TAG, "Removing voicemail at position [" + position + "]");
                    voicemails.remove(position);

                    new VoicemailParser().onPostExecute(voicemails);
                }
                break;
            }
        }
    }
    
    private class VoicemailParser extends AsyncTask<String, Integer, List<Voicemail>>
    {
        @Override
        protected List<Voicemail> doInBackground(String... strings)
        {
            Log.v(TAG, "VoicemailParser.doInBackground()");
            try
            {
                return serviceBinder.getService().getVoicemails();
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Error getting Voicemails from service", e);
                return new ArrayList<Voicemail>();
            }
        }

        @Override
        protected void onPostExecute(List<Voicemail> voicemails)
        {
            Log.v(TAG, "VoicemailParser.onPostExecute()");
            try
            {
                long[] unnotifiedIds = filterUnnotifiedVoicemails(voicemails);

                if(unnotifiedIds.length > 0)
                {
                    serviceBinder.getService().markVoicemailsNotified(unnotifiedIds);
                }

                int numNew = 0;

                VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
                adapter.setData(voicemails);
                for(Voicemail voicemail : (List<Voicemail>)voicemails)
                {
                    if(voicemail.getIsNew())
                    {
                        numNew++;
                    }
                }

                TextView inboxStatus = (TextView)findViewById(R.id.inboxStatus);
                inboxStatus.setText(ListenVoicemail.this.getString(R.string.current_status) + " (" + numNew + "/" +
                                    voicemails.size() + ")");
            }
            catch(RemoteException e)
            {
                Log.e("TAG", "Error updating list view with latest info: ", e);
            }
        }
    }

    private class VoicemailListAdapter extends ArrayAdapter<Object>
    {
        private List<Voicemail> mVoicemails = new ArrayList<Voicemail>();

        public VoicemailListAdapter(List<Voicemail> items)
        {
            super(ListenVoicemail.this, R.layout.voicemail);
            Log.v(TAG, "new VoicemailListAdapter()");
            mVoicemails = items;
        }

        public void setData(List<Voicemail> voicemails)
        {
            Log.v(TAG, "VoicemailListAdapter.setData()");
            mVoicemails = voicemails;
            clear();
            for(Voicemail voicemail : mVoicemails)
            {
                add(voicemail);
            }
        }

        public List<Voicemail> getData()
        {
            Log.v(TAG, "VoicemailListAdapter.getData()");
            return mVoicemails;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Log.v(TAG, "VoicemailListAdapter.getView()");
            ViewHolder holder;
            LayoutInflater mInflater = getLayoutInflater();

            if(convertView == null)
            {
                convertView = mInflater.inflate(R.layout.voicemail, null);

                holder = new ViewHolder();
                holder.leftBy = (TextView)convertView.findViewById(R.id.leftBy);
                holder.date = (TextView)convertView.findViewById(R.id.date);
                holder.transcription = (TextView)convertView.findViewById(R.id.transcription);

                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.leftBy.setText(mVoicemails.get(position).getLeftBy());
            holder.date.setText(mVoicemails.get(position).getDateCreated());

            String transcription = getTruncatedTranscription(mVoicemails.get(position).getTranscription());
            holder.transcription.setText(transcription);

            final Typeface typeface;

            if(mVoicemails.get(position).getIsNew())
            {
                typeface = Typeface.defaultFromStyle(Typeface.BOLD);
            }
            else
            {
                typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
            }

            holder.leftBy.setTypeface(typeface);
            holder.date.setTypeface(typeface);
            holder.transcription.setTypeface(typeface);

            return convertView;
        }

        private String getTruncatedTranscription(String fullTranscription)
        {
            StringBuilder returnString = new StringBuilder("");
            if(fullTranscription != null && !fullTranscription.equals(""))
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

        private class ViewHolder
        {
            TextView leftBy;
            TextView date;
            TextView transcription;
        }
    }

    private void clearNotificationBar()
    {
        Log.v(TAG, "clearNotificationBar()");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(ns);

        mNotificationManager.cancel(LISTEN_NOTIFICATION);
    }

    private long[] filterUnnotifiedVoicemails(List<Voicemail> voicemails)
    {
        Log.v(TAG, "filterUnnotifiedVoicemails()");
        List<Voicemail> tempVoicemails = new ArrayList<Voicemail>(mVoicemails);

        for(Voicemail voicemail : voicemails)
        {
            if(!voicemail.getHasNotified())
            {
                tempVoicemails.add(voicemail);
            }
        }

        long[] returnArray = new long[tempVoicemails.size()];

        for(int i = 0; i < tempVoicemails.size(); i++)
        {
            returnArray[i] = tempVoicemails.get(i).getId();
        }

        return returnArray;
    }
}
