package com.interact.listen.android.voicemail;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListenVoicemail extends ListActivity
{
    private static final String TAG = "ListenVoicemailActivity";
    private static final int VIEW_DETAILS = 0;
    private static final int EDIT_SETTINGS = 1;
    private static final int LISTEN_NOTIFICATION = 45;
    private static final int MENU_SETTINGS_ID = Menu.FIRST;
    private IListenVoicemailService remoteService;
    private boolean started = false;
    private RemoteServiceConnection conn = null;
    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    private ArrayList<Voicemail> mVoicemails = new ArrayList<Voicemail>();
    
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received broadcast");
            abortBroadcast();
            try
            {
                new VoicemailParser().onPostExecute((ArrayList<Voicemail>)remoteService.getVoicemails());
            }
            catch(RemoteException e)
            {
                Log.e(TAG, "Error retreiving voicemails from the service: " + e);
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        startService();
        bindService();
        startService(new Intent(this, ListenVoicemailService.class));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        VoicemailListAdapter adapter = new VoicemailListAdapter(new ArrayList<Voicemail>(0));
        setListAdapter(adapter);
        
        new VoicemailParser().execute("");
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume()");
        new VoicemailParser().execute("");
        clearNotificationBar();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION_STRING);
        filter.setPriority(2);

        this.registerReceiver(this.receiver, filter);
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        this.unregisterReceiver(this.receiver);
        Log.v("TONY", "onPause()");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        releaseService();
        Log.d(getClass().getSimpleName(), "onDestroy()");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SETTINGS_ID, 0, R.string.menu_settings);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case MENU_SETTINGS_ID:
            Intent i = new Intent(this, ApplicationSettings.class);
            startActivityForResult(i, VIEW_DETAILS);
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        Voicemail voicemail = (Voicemail)getListAdapter().getItem(position);
        Intent i = new Intent(this, VoicemailDetails.class);
        i.putExtra("id", voicemail.getId());
        i.putExtra("leftBy", voicemail.getLeftBy());
        i.putExtra("date", voicemail.getDateCreated());
        i.putExtra("transcription", voicemail.getTranscription());
        i.putExtra("position", position);
        startActivityForResult(i, VIEW_DETAILS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode)
        {
            case VIEW_DETAILS:
            {
                // intent will be null if the 'Back' button is pressed
                if(intent != null)
                {
                    Bundle extras = intent.getExtras();
                    int position = extras.getInt("position");
                    boolean deleted = extras.getBoolean("deleted");
                    VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
                    ArrayList<Voicemail> voicemails = adapter.getData();
                    Voicemail updatedVoicemail = voicemails.get(position);

                    if(deleted)
                    {
                        voicemails.remove(position);
                    }
                    else
                    {
                        updatedVoicemail.setIsNew(false);
                    }

                    new VoicemailParser().onPostExecute(voicemails);
                }
                break;
            }
        }
    }

    private class VoicemailParser extends AsyncTask<String, Integer, ArrayList<Voicemail>>
    {
        public VoicemailParser()
        {}

        @Override
        protected ArrayList<Voicemail> doInBackground(String... strings)
        {
            try
            {
                ResponseHandler<String> handler = new ResponseHandler<String>()
                {
                    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
                    {
                        HttpEntity entity = response.getEntity();
                        if(entity != null)
                        {
                            return EntityUtils.toString(entity);
                        }
                        else
                        {
                            return null;
                        }
                    }
                };

                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 3000);

                HttpClient httpClient = new DefaultHttpClient(httpParams);
                HttpGet httpGet = new HttpGet("http://192.168.1.221:9090/api/voicemails?subscriber=/subscribers/26" +
                                              "&_fields=id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified" +
                                              "&_sortBy=dateCreated&_sortOrder=DESCENDING");
                
                httpGet.addHeader("Accept", "application/json");
                String response = httpClient.execute(httpGet, handler);
                // Log.v("TONY", response);

                JSONObject jsonObj = new JSONObject(response);
                int total = Integer.valueOf(jsonObj.getString("total"));

                JSONArray voicemailArray = jsonObj.getJSONArray("results");

                ArrayList<Voicemail> voicemails = new ArrayList<Voicemail>(total);
                for(int i = 0; i < total; i++)
                {
                    JSONObject object = voicemailArray.getJSONObject(i);
                    voicemails.add(new Voicemail(object.getString("id"), object.getString("isNew"),
                                                 object.getString("leftBy"), object.getString("description"),
                                                 object.getString("dateCreated"), object.getString("duration"),
                                                 object.getString("transcription"),
                                                 object.getString("hasNotified")));
                }

                return voicemails;
            }
            catch(Exception e)
            {
                Log.e("TONY", "Exception getting JSON data", e);
                return new ArrayList<Voicemail>();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Voicemail> voicemails)
        {
            try
            {
                long[] unnotifiedIds = filterUnnotifiedVoicemails(voicemails);
                
                Log.d("TONY", "unnotifiedIds.length = " + unnotifiedIds.length);
                if(unnotifiedIds.length > 0)
                {
                    remoteService.updateNotificationStatus(unnotifiedIds);
                }
                
                int numNew = 0;

                VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
                adapter.clear();
                adapter.setData(voicemails);
                for(Voicemail voicemail : (ArrayList<Voicemail>)voicemails)
                {
                    if(voicemail.getIsNew())
                    {
                        numNew++;
                    }
                    
                    adapter.add(voicemail);
                }
                
                TextView inboxStatus = (TextView)findViewById(R.id.inboxStatus);
                inboxStatus.setText(ListenVoicemail.this.getString(R.string.current_status) + " (" + numNew + "/" + voicemails.size() + ")");
            }
            catch(Exception e)
            {
                Log.e("TAG", "Error updating list view with latest info: " + e);
            }
        }
    }

    private class VoicemailListAdapter extends ArrayAdapter<Object>
    {
        private ArrayList<Voicemail> mVoicemails = new ArrayList<Voicemail>();

        public VoicemailListAdapter(ArrayList<Voicemail> items)
        {
            super(ListenVoicemail.this, R.layout.voicemail);
            mVoicemails = items;
        }

        public void setData(ArrayList<Voicemail> voicemails)
        {
            mVoicemails = voicemails;
        }

        public ArrayList<Voicemail> getData()
        {
            return mVoicemails;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
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
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(ns);
        
        mNotificationManager.cancel(LISTEN_NOTIFICATION);
    }
    
    private long[] filterUnnotifiedVoicemails(ArrayList<Voicemail> voicemails)
    {
        ArrayList<Voicemail> tempVoicemails = new ArrayList<Voicemail>(mVoicemails);
        
        for(Voicemail voicemail : voicemails)
        {
            Log.d("TONY", "voicemail " + voicemail.getId() + " getHasNotified() = " + voicemail.getHasNotified());
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

    class RemoteServiceConnection implements ServiceConnection
    {
        public void onServiceConnected(ComponentName className, IBinder boundService)
        {
            remoteService = IListenVoicemailService.Stub.asInterface((IBinder)boundService);
            Log.d(getClass().getSimpleName(), "onServiceConnected()");
        }

        public void onServiceDisconnected(ComponentName className)
        {
            remoteService = null;
            Log.d(getClass().getSimpleName(), "onServiceDisconnected");
        }
    };
    
    private void startService()
    {
        if(started)
        {
            Log.i(TAG, "Service already started");
        }
        else
        {
            Intent i = new Intent();
            i.setClassName("com.interact.listen.android.voicemail", "com.interact.listen.android.voicemail.ListenVoicemailService");
            startService(i);
            started = true;
            Log.d(TAG, "startService()");
        }

    }

    private void releaseService()
    {
        if(conn != null)
        {
            unbindService(conn);
            conn = null;
            Log.d(TAG, "releaseService()");
        }
        else
        {
            Log.i(TAG, "Cannot unbind - service not bound");
        }
    }
    
    private void stopService()
    {
        if(!started)
        {
            Log.i(TAG, "Service not yet started");
        }
        else
        {
            Intent i = new Intent();
            i.setClassName("com.interact.listen.android.voicemail", "com.interact.listen.android.voicemail.ListenVoicemailService");
            stopService(i);
            started = false;
            Log.d(getClass().getSimpleName(), "stopService()");
        }
    }

    private void bindService()
    {
        if(conn == null)
        {
            conn = new RemoteServiceConnection();
            Intent i = new Intent();
            i.setClassName("com.interact.listen.android.voicemail", "com.interact.listen.android.voicemail.ListenVoicemailService");
            bindService(i, conn, Context.BIND_AUTO_CREATE);
            Log.d(getClass().getSimpleName(), "bindService()");
        }
        else
        {
            Log.i(TAG, "Cannot bind - service already bound");
        }
    }
}
