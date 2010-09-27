package com.interact.listen.android.voicemail;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UpdatedVoicemailReceiver extends BroadcastReceiver
{
    private long[] ids;
    private int newMessageCount;
    private Context mContext;
    private static final int LISTEN_NOTIFICATION = 45;
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle extras = intent.getExtras();
        ids = extras.getLongArray("ids");
        newMessageCount = extras.getInt("newMessageCount");
        
        Log.d("TONY", "ids = " + Arrays.toString(ids));
        
        mContext = context;
        updateNotifications();
        updateVoicemailNotificationStatus();
    }
    
    private void updateNotifications()
    {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager)mContext.getSystemService(ns);
        int idsLength = ids.length;
        
        if(idsLength > 0)
        {
            CharSequence tickerText = idsLength > 1 ? "New Listen Voicemails" : "New Listen Voicemail";
            CharSequence contentTitle = idsLength > 1 ? "New Listen Voicemails" : "New Listen Voicemail";
            CharSequence contentText = newMessageCount + " new " + (newMessageCount > 1 ?  "Voicemails" : "Voicemail");

            int icon = R.drawable.notification_bar_icon;
            long when = System.currentTimeMillis();
            Notification notification = new Notification(icon, tickerText, when);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            Intent notificationIntent = new Intent(mContext, ListenVoicemail.class);
            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
            notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
            //If there is only one new message, don't show the number over the icon
            //notification.number = idsLength > 1 ? idsLength : 0;
            mNotificationManager.notify(LISTEN_NOTIFICATION, notification);
        }
    }
    
    private void updateVoicemailNotificationStatus()
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
            
                httpClient.execute(httpPut, handler);
            }
        }
        catch(Exception e)
        {
            Log.e("TONY", "Exception updating voicemail read status", e);
        }
    }
}
