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
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListenVoicemail extends ListActivity {
	private static final int VIEW_DETAILS=0;
	private Handler mHandler = new Handler();
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   try
       		   {
       			    new VoicemailParser().execute("");
       		   }
			   catch(Exception e)
       		   {
       				Log.e("TONY", "Exception getting JSON data", e);
       		   }
			   
		       mHandler.postDelayed(this, 12000);
		   }
		};
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        VoicemailListAdapter adapter = new VoicemailListAdapter(new ArrayList<Voicemail>(0));
        setListAdapter(adapter);
        
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
		switch (requestCode)
		{
			case VIEW_DETAILS:
			{
				//intent will be null if the 'Back' button is pressed
				if(intent != null)
				{
					Bundle extras = intent.getExtras();
			        int position = extras.getInt("position");
			        boolean deleted = extras.getBoolean("deleted");
					VoicemailListAdapter adapter = (VoicemailListAdapter) getListAdapter();
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
    
    private class VoicemailParser extends AsyncTask<String, Integer, ArrayList<Voicemail>>{
    	public VoicemailParser()
    	{}
    	
    	@Override
		protected ArrayList<Voicemail> doInBackground(String... strings) {
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
            	HttpGet httpGet = new HttpGet("http://picard.interact.nonreg:9090/api/voicemails?subscriber=/subscribers/26" +
            			"&_fields=id,isNew,leftBy,description,dateCreated,duration,transcription&_sortBy=dateCreated&_sortOrder=DESCENDING");
            	httpGet.addHeader("Accept", "application/json");
            	String response = httpClient.execute(httpGet, handler);
            	//Log.v("TONY", response);
            	
            	JSONObject jsonObj = new JSONObject(response);
            	int total = Integer.valueOf(jsonObj.getString("total"));
            	
                JSONArray voicemailArray = jsonObj.getJSONArray("results");
                
                ArrayList<Voicemail> voicemails = new ArrayList<Voicemail>(total);
                for (int i = 0; i < total; i++)
                {
    				JSONObject object = voicemailArray.getJSONObject(i);
    				voicemails.add(new Voicemail(
    						object.getString("id"),
    						object.getString("isNew"), 
    						object.getString("leftBy"), 
    						object.getString("description"),
    						object.getString("dateCreated"),
    						object.getString("duration"),
    						object.getString("transcription")));
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
		protected void onPostExecute(ArrayList<Voicemail> voicemails){
        	int numNew = 0;
    		
    		for(Voicemail voicemail : voicemails)
    		{
    			if(voicemail.getIsNew())
    			{
    				numNew++;
    			}
    		}
    		
    		TextView inboxStatus = (TextView)findViewById(R.id.inboxStatus);
    		inboxStatus.setText(ListenVoicemail.this.getString(R.string.current_status) + " (" + numNew + "/" + voicemails.size() + ")");
    		
    		VoicemailListAdapter adapter = (VoicemailListAdapter)getListAdapter();
    		adapter.clear();
    		adapter.setData(voicemails);
    		for(Voicemail voicemail : voicemails)
    		{
    			adapter.add(voicemail);
    		}
		}
    }
    
    private class VoicemailListAdapter extends ArrayAdapter<Object> {
    	private ArrayList<Voicemail> mVoicemails = new ArrayList<Voicemail>();
    	
		public VoicemailListAdapter(ArrayList<Voicemail> items) {
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

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			LayoutInflater mInflater = getLayoutInflater();

			if (convertView == null)
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
			holder.leftBy.setText( mVoicemails.get(position).getLeftBy());
			holder.date.setText( mVoicemails.get(position).getDateCreated());
			
			String transcription = getTruncatedTranscription(mVoicemails.get(position).getTranscription());
			holder.transcription.setText(transcription);
			
			final Typeface typeface;
			
			if(mVoicemails.get(position).getIsNew())
			{
				typeface = Typeface.defaultFromStyle (Typeface.BOLD);
			}
			else
			{
				typeface = Typeface.defaultFromStyle (Typeface.NORMAL);
			}
			
			holder.leftBy.setTypeface( typeface );
			holder.date.setTypeface( typeface );
			holder.transcription.setTypeface( typeface );
			
			return convertView;
		}
		
		private String getTruncatedTranscription(String fullTranscription)
		{
			StringBuilder returnString = new StringBuilder("");
			if(fullTranscription != null && !fullTranscription.equals(""))
			{
				//add the transcription to what we will return
				returnString.append(fullTranscription);
				if(fullTranscription.length() > 45)
				{
					//Add ... and only show the first 45 characters
					return returnString.insert(42, "...").substring(0, 45); 
				}
			}
			
			return returnString.toString();
		}

		private class ViewHolder {
			TextView leftBy;
			TextView date;
			TextView transcription;
		}
	}
}