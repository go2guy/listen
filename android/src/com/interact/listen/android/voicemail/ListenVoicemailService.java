package com.interact.listen.android.voicemail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class ListenVoicemailService extends Service implements OnSharedPreferenceChangeListener
{
    private static final String TAG = "ListenVoicemailService";
    //private static final int LISTEN_NOTIFICATION = 45;
    private Handler serviceHandler;
    private Task myTask = new Task();
    private List<Voicemail> mVoicemails = new ArrayList<Voicemail>(10);
    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    private int mCurrentSubscriberId;
    private String mCurrentHostPort;
    
    private SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(TAG, "onBind()");
        return myListenVoicemailServiceStub;
    }

    private IListenVoicemailService.Stub myListenVoicemailServiceStub = new IListenVoicemailService.Stub()
    {
        public List<Voicemail> getVoicemails() throws RemoteException
        {
            return mVoicemails;
        }
        
        public boolean updateNotificationStatus(long[] ids)
        {
            //Stop the polling, update notification status, start the polling again
            serviceHandler.removeCallbacks(myTask);
            updateVoicemailNotificationStatus(ids);
            serviceHandler.postDelayed(myTask, 100L);
            
            return true;
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        serviceHandler.removeCallbacks(myTask);
        serviceHandler = null;
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        
        serviceHandler = new Handler();
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setConnectionInformation();
        
        serviceHandler.postDelayed(myTask, 100L);
        
        Log.d(TAG, "onStart()");
    }

    class Task implements Runnable
    {
        public void run()
        {
            Log.i(TAG, "Getting voicemails...");
            getVoicemails();
            Bundle bundle = new Bundle();
            bundle.putLongArray("ids", getIdsToUpdate());
            bundle.putInt("newMessageCount", getNewMessageCount());
            
            Intent i = new Intent();
            i.setAction(UPDATE_ACTION_STRING);
            i.putExtras(bundle);
            
            sendOrderedBroadcast(i, null);
            
            //updateNotifications();
            serviceHandler.postDelayed(this, 30000L);
        }
    }

    private void getVoicemails()
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
            HttpGet httpGet = new HttpGet("http://" + mCurrentHostPort + "/api/voicemails?subscriber=/subscribers/" + mCurrentSubscriberId + 
                                              "&_fields=id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified" +
                                              "&_sortBy=dateCreated&_sortOrder=DESCENDING");
            
            httpGet.addHeader("Accept", "application/json");
            Log.d(TAG, "Trying to get voicemails from " + httpGet.getURI().toString());
            String response = httpClient.execute(httpGet, handler);

            JSONObject jsonObj = new JSONObject(response);
            int total = Integer.valueOf(jsonObj.getString("total"));

            JSONArray voicemailArray = jsonObj.getJSONArray("results");

            mVoicemails = new ArrayList<Voicemail>(total);
            for(int i = 0; i < total; i++)
            {
                JSONObject object = voicemailArray.getJSONObject(i);
                mVoicemails.add(new Voicemail(object.getString("id"),
                                              object.getString("isNew"),
                                              object.getString("leftBy"),
                                              object.getString("description"),
                                              object.getString("dateCreated"),
                                              object.getString("duration"),
                                              object.getString("transcription"),
                                              object.getString("hasNotified")));
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error getting voicemails :" + e);
        }
    }
    
    private long[] getIdsToUpdate()
    {
        //Should protect us from a poll of the server disrupting the looping we do below
        ArrayList<Voicemail> voicemails = new ArrayList<Voicemail>(mVoicemails);
        ArrayList<Voicemail> tempVoicemails = new ArrayList<Voicemail>();
        Collections.copy(voicemails, mVoicemails);
        
        for(Voicemail voicemail : voicemails)
        {
            if(!voicemail.getHasNotified())
            {
                tempVoicemails.add(voicemail);
            }
        }
        
        long[] idsToUpdate = new long[tempVoicemails.size()];
        for(int i = 0; i < tempVoicemails.size(); i++)
        {
            idsToUpdate[i] = tempVoicemails.get(i).getId();
        }
        
        return idsToUpdate;
    }
    
    private int getNewMessageCount()
    {
        int numNew = 0;
        
        for(Voicemail voicemail : mVoicemails)
        {
            if(voicemail.getIsNew())
            {
                numNew++;
            }
        }
        
        return numNew;
    }
    
    private void updateVoicemailNotificationStatus(long[] ids)
    {
        try
        {
            ResponseHandler<String> handler = new ResponseHandler<String>()
            {
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException
                {
                    HttpEntity entity = response.getEntity();
                    if (entity != null)
                    {
                        return EntityUtils.toString(entity);
                    }
                    else
                    {
                        return null;
                    }
                }
            };
            
            JSONObject voicemail = new JSONObject();            
            voicemail.put("hasNotified", true);
            
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            HttpConnectionParams.setSoTimeout(httpParams, 3000);
            
            HttpClient httpClient = new DefaultHttpClient(httpParams);
            
            for(int i = 0; i < ids.length; i++)
            {
                HttpPut httpPut = new HttpPut("http://" + mCurrentHostPort + "/api/voicemails/" + String.valueOf(ids[i]));
            
                HttpEntity entity = new StringEntity(voicemail.toString(), "UTF-8");
                httpPut.setEntity(entity);
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-Type", "application/json");
            
                httpClient.execute(httpPut, handler);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Exception updating voicemail read status", e);
        }
    }
    
    private void setConnectionInformation()
    {
        mCurrentHostPort = getIpFromHostname(sharedPreferences.getString(ApplicationSettings.KEY_HOST_PREFERENCE, "")) + ":" + 
            sharedPreferences.getString(ApplicationSettings.KEY_PORT_PREFERENCE, "");
        
        mCurrentSubscriberId = getSubscriberId(sharedPreferences.getString(ApplicationSettings.KEY_USERNAME_PREFERENCE, "user"));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ApplicationSettings.KEY_SUBSCRIBER_ID_PREFERENCE, mCurrentSubscriberId);
        
        editor.commit();
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        //Stop polling while we update to the newest settings information
        serviceHandler.removeCallbacks(myTask);
        
        if(key.equals(ApplicationSettings.KEY_HOST_PREFERENCE))
        {
            mCurrentHostPort = getIpFromHostname(sharedPreferences.getString(key, "")) + ":" +
                               sharedPreferences.getString(ApplicationSettings.KEY_PORT_PREFERENCE, "");
        }
        else if(key.equals(ApplicationSettings.KEY_PORT_PREFERENCE))
        {
            mCurrentHostPort = sharedPreferences.getString(ApplicationSettings.KEY_HOST_PREFERENCE, "") + ":" +
                               sharedPreferences.getString(key, "");
        }
        else if(key.equals(ApplicationSettings.KEY_USERNAME_PREFERENCE))
        {
            mCurrentSubscriberId = getSubscriberId(sharedPreferences.getString(key, "user"));
            Log.d(TAG, "setting mCurrentSubscriberId to " + mCurrentSubscriberId + " due to settings change");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(ApplicationSettings.KEY_SUBSCRIBER_ID_PREFERENCE, mCurrentSubscriberId);
            
            editor.commit();
        }
        
        //resume polling
        serviceHandler.postDelayed(myTask, 100L);
    }
    
    private int getSubscriberId(String username)
    {
        int subscriberId = 0;
        
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
            HttpGet httpGet = new HttpGet("http://" + mCurrentHostPort + "/api/subscribers?username=" + username + "&_fields=id");
            
            httpGet.addHeader("Accept", "application/json");
            Log.d(TAG, "obtaining user id for " + username + " from " + httpGet.getURI().toString());
            String response = httpClient.execute(httpGet, handler);

            JSONObject jsonObj = new JSONObject(response);
            int total = Integer.valueOf(jsonObj.getString("total"));

            JSONArray subscriberArray = jsonObj.getJSONArray("results");

            if(total == 1)
            {
                JSONObject subscriber = subscriberArray.getJSONObject(0);
                subscriberId = Integer.valueOf(subscriber.getString("id"));
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error getting subscriber id from username:" + e);
        }
        
        return subscriberId;
    }
    
    private String getIpFromHostname(String hostname)
    {
        try
        {
            InetAddress internetAddress = InetAddress.getByName(hostname);
            return internetAddress.getHostAddress();
        }
        catch(UnknownHostException e)
        {
            //Log, but let it through. Polling errors will alert the user that what they entered is incorrect
            Log.i(TAG, "Caught UnknownHostException trying to determine ip address from hostname: " + hostname);
            Log.e(TAG, e.toString());
            return hostname;
        }
    }
}
