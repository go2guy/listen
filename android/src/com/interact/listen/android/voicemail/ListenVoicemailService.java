package com.interact.listen.android.voicemail;

import java.io.IOException;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ListenVoicemailService extends Service
{
    private static final String TAG = "ListenVoicemailService";
    //private static final int LISTEN_NOTIFICATION = 45;
    private Handler serviceHandler;
    private Task myTask = new Task();
    private List<Voicemail> mVoicemails = new ArrayList<Voicemail>(10);
    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";

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
        serviceHandler.removeCallbacks(myTask);
        serviceHandler = null;
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        serviceHandler = new Handler();
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
            HttpGet httpGet = new HttpGet("http://192.168.1.221:9090/api/voicemails?subscriber=/subscribers/26" +
                                              "&_fields=id,isNew,leftBy,description,dateCreated,duration,transcription,hasNotified" +
                                              "&_sortBy=dateCreated&_sortOrder=DESCENDING");
            
            httpGet.addHeader("Accept", "application/json");
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
            Log.e(TAG, "Error getting voicemail count:" + e);
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
                HttpPut httpPut = new HttpPut("http://192.168.1.221:9090/api/voicemails/" + String.valueOf(ids[i]));
            
                HttpEntity entity = new StringEntity(voicemail.toString(), "UTF-8");
                httpPut.setEntity(entity);
                httpPut.setHeader("Accept", "application/json");
                httpPut.setHeader("Content-Type", "application/json");
            
                Log.v("TONY", "sending request " + httpPut.toString());
            
                String response = httpClient.execute(httpPut, handler);
                
                Log.v("TONY", "response when updating hasNotified = " + response);
            }
        }
        catch(Exception e)
        {
            Log.e("TONY", "Exception updating voicemail read status", e);
        }
    }
}
