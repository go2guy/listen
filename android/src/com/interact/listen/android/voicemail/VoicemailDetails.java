package com.interact.listen.android.voicemail;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VoicemailDetails extends Activity
{
    private static final String TAG = "ListenVoicemailDetails";
    private TextView mLeftBy;
    private TextView mDate;
    private TextView mTranscription;
    private long mVoicemailId;
    private int mPosition;
    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received broadcast");
            //abortBroadcast();
            //do nothing, currently we want the notification to appear
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicemail_details);
        
        /*if(savedInstanceState == null)
        {
        	// We just want to go back to the list of voicemails
        	setResult(RESULT_OK);
            finish();
        }*/
        
        Bundle extras =  getIntent().getExtras();
        mVoicemailId = extras.getLong("id");
        String leftBy = extras.getString("leftBy");
        String date = extras.getString("date");
        String transcription = extras.getString("transcription");
        mPosition = extras.getInt("position");
        
        mLeftBy = (TextView) findViewById(R.id.detailLeftBy);
        mDate = (TextView) findViewById(R.id.detailDate);
        mTranscription = (TextView) findViewById(R.id.detailTranscription);
        
        mLeftBy.setText(leftBy);
        mDate.setText(date);
        mTranscription.setText(transcription);
        
        Button markReadButton = (Button) findViewById(R.id.markRead);
        Button deleteButton = (Button) findViewById(R.id.delete);
        
        markReadButton.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		try
        		{
        			new MarkVoicemailRead().execute(mVoicemailId);
        			Bundle bundle = new Bundle();
        			bundle.putInt("position", mPosition);
        			bundle.putBoolean("deleted", false);
        			
        			Intent mIntent = new Intent();
        			mIntent.putExtras(bundle);
        			
        			setResult(RESULT_OK, mIntent);
                    finish();
        		} catch(Exception e) {
        			Log.e("TONY", "Exception marking voicemail read", e);
        		}
        	}
        });
        
        deleteButton.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		try
        		{
        			new DeleteVoicemail().execute(mVoicemailId);
        			Bundle bundle = new Bundle();
        			bundle.putInt("position", mPosition);
        			bundle.putBoolean("deleted", true);
        			
        			Intent mIntent = new Intent();
        			mIntent.putExtras(bundle);
        			
        			setResult(RESULT_OK, mIntent);
                    finish();
        		} catch(Exception e) {
        			Log.e("TONY", "Exception deleting voicemail", e);
        		}
        	}
        });
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION_STRING);
        filter.setPriority(2);

        this.registerReceiver(this.receiver, filter);
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d("TONY", "details onPause()");
        this.unregisterReceiver(this.receiver);
    }
    
    private class MarkVoicemailRead extends AsyncTask<Long, Integer, Void>{
    	public MarkVoicemailRead()
    	{}
    	
    	@Override
		protected Void doInBackground(Long... voicemailIds) {
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
            	
        		HttpParams httpParams = new BasicHttpParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            	HttpConnectionParams.setSoTimeout(httpParams, 3000);
            	
            	HttpClient httpClient = new DefaultHttpClient(httpParams);
            	
            	HttpPut httpPut = new HttpPut("http://picard.interact.nonreg:9090/api/voicemails/" + String.valueOf(voicemailIds[0]));
            	JSONObject voicemail = new JSONObject();        	
            	voicemail.put("isNew", false);
            	
            	HttpEntity entity = new StringEntity(voicemail.toString(), "UTF-8");
            	httpPut.setEntity(entity);
            	httpPut.setHeader("Accept", "application/json");
            	httpPut.setHeader("Content-Type", "application/json");
            	
            	Log.v("TONY", "sending request " + httpPut.toString());
            	
            	httpClient.execute(httpPut, handler);
            	
            	return null;
    		}
    		catch(Exception e)
    		{
    			Log.e("TONY", "Exception updating voicemail read status", e);
    			return null;
    		}
    	}
    }
    
    private class DeleteVoicemail extends AsyncTask<Long, Integer, Void>{
    	public DeleteVoicemail()
    	{}
    	
    	@Override
		protected Void doInBackground(Long... voicemailIds) {
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
            	
        		HttpParams httpParams = new BasicHttpParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
            	HttpConnectionParams.setSoTimeout(httpParams, 3000);
            	
            	HttpClient httpClient = new DefaultHttpClient(httpParams);
            	
            	HttpDelete httpDelete = new HttpDelete("http://picard.interact.nonreg:9090/api/voicemails/" + String.valueOf(voicemailIds[0]));
            	httpDelete.setHeader("Accept", "application/json");
            	httpDelete.setHeader("Content-Type", "application/json");
            	
            	Log.v("TONY", "sending request " + httpDelete.toString());
            	
            	httpClient.execute(httpDelete, handler);
            	
            	return null;
    		}
    		catch(Exception e)
    		{
    			Log.e("TONY", "Exception updating voicemail read status", e);
    			return null;
    		}
    	}
    }
}
