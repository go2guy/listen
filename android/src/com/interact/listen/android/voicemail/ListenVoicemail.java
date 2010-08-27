package com.interact.listen.android.voicemail;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ListenVoicemail extends ListActivity {
	
	Handler h;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //final TextView textView = (TextView)findViewById(R.id.newVoicemails);
        
        final Button button = (Button)findViewById(R.id.ButtonGo);
        button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		try
        		{
        			new VoicemailParser().execute("");
        			
        			/*String numNewMessages = "0";
        			textView.setText("");
        			
        			URL connectURL = new URL("http://192.168.1.221:8080/api/voicemails?subscriber=/subscribers/26");
            		HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection(); 

            		// do some setup
            		conn.setRequestMethod("GET");

            		// connect and flush the request out
            		conn.connect();

            		// now fetch the results
            		String response = getResponse(conn);
            		
            		response = response.replace("total=\"", "@");
                    response = response.substring(response.indexOf("@"));
                    numNewMessages = response.substring(response.indexOf("@") + 1, response.indexOf("\""));
                    
            		textView.setText(numNewMessages);*/
            		
        		} catch(Exception e) {
        			Log.e("TONY", "Exception getting JSON data", e);
        		}
        	}
        	
        	private String getResponse(HttpURLConnection conn)
        	{
        	    InputStream is = null;
        	    try 
        	    {
        	        is = conn.getInputStream(); 
        	        // scoop up the reply from the server
        	        int ch; 
        	        StringBuffer sb = new StringBuffer(); 
        	        while( ( ch = is.read() ) != -1 ) {
        	        	sb.append( (char)ch ); 
        	        } 
        	        return sb.toString(); 
        	    }
        	    catch(Exception e)
        	    {
        	    	Log.v("TONY", "Error getting response: " + e.getStackTrace());
        	    }
        	    finally 
        	    {
        	        try {
        	        if (is != null)
        	            is.close();
        	        } catch (Exception e) {}
        	    }

        	    return "";
        	}
        });
    }
    
    private class VoicemailParser extends AsyncTask<String, Integer, Voicemail[]>{
    	public VoicemailParser()
    	{}
    	
    	@Override
		protected Voicemail[] doInBackground(String... symbols) {
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
            	HttpGet httpget = new HttpGet("http://picard.interact.nonreg:9090/api/voicemails?subscriber=/subscribers/26" +
            			"&_fields=isNew,leftBy,description,dateCreated,duration,transcription&_sortBy=dateCreated&_sortOrder=DESCENDING");
            	httpget.addHeader("Accept", "application/json");
            	String response = httpClient.execute(httpget, handler);
            	Log.v("TONY", response);
            	
            	JSONObject jsonObj = new JSONObject(response);
            	int total = Integer.valueOf(jsonObj.getString("total"));
            	
                JSONArray voicemailArray = jsonObj.getJSONArray("results");
                
                Voicemail[] voicemails = new Voicemail[Integer.valueOf(total)];
                for (int i = 0; i < voicemails.length; i++)
                {
    				JSONObject object = voicemailArray.getJSONObject(i);
    				voicemails[i] = new Voicemail(
    						object.getString("isNew"), object.getString("leftBy"), 
    						object.getString("description"),
    						object.getString("dateCreated"),
    						object.getString("duration"),
    						object.getString("transcription"));
                }
                
                return voicemails;
    		}
    		catch(Exception e)
    		{
    			Log.e("TONY", "Exception getting JSON data", e);
    			return new Voicemail[0];
    		}
    	}
    	
    	@Override
		protected void onPostExecute(Voicemail[] voicemails){
        	//ArrayAdapter<Voicemail> adapter = new ArrayAdapter<Voicemail>(ListenVoicemail.this, R.layout.voicemail, voicemails);
    		int numNew = 0;
    		
    		for(Voicemail voicemail : voicemails)
    		{
    			if(voicemail.getIsNew())
    			{
    				numNew++;
    			}
    		}
    		
    		TextView inboxStatus = (TextView)findViewById(R.id.inboxStatus);
    		inboxStatus.setText(ListenVoicemail.this.getString(R.string.current_status) + " (" + numNew + "/" + voicemails.length + ")");
    		
    		VoicemailListAdapter adapter = new VoicemailListAdapter(voicemails);
            setListAdapter(adapter);
		}
    }
    
    private class VoicemailListAdapter extends ArrayAdapter<Object> {
    	Voicemail[] mVoicemails;
    	
		public VoicemailListAdapter(Voicemail[] items) {
			super(ListenVoicemail.this, R.layout.voicemail, items);
			mVoicemails = items;
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
				//holder.transcription = (TextView)convertView.findViewById(R.id.transcription);
			
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
			
			// Bind the data efficiently with the holder.
			holder.leftBy.setText( mVoicemails[position].getLeftBy());
			holder.date.setText( mVoicemails[position].getDateCreated());
			//holder.transcription.setText(mVoicemails[position].getTranscription()
								//.equals("") ? "Transcription not available" : mVoicemails[position].getTranscription());
			
			final Typeface typeface;
			
			if(mVoicemails[position].getIsNew())
			{
				typeface = Typeface.defaultFromStyle (Typeface.BOLD);
			}
			else
			{
				typeface = Typeface.defaultFromStyle (Typeface.NORMAL);
			}
			
			//holder.leftBy.setTextSize( 18 );
			//holder.date.setTextSize( 14 );
			//holder.transcription.setTextSize( 12 );
			holder.leftBy.setTypeface( typeface );
			holder.date.setTypeface( typeface );
			//holder.transcription.setTypeface( typeface );
			
			/*switch ( position % 6 ) {
			case 0:
				holder.leftBy.setTextColor( 0xFFFF0000 );
				holder.date.setTextColor( 0xFFFF0000 );
				//holder.transcription.setTextColor( 0xFFFF0000 );
				break;
			case 1:
				holder.leftBy.setTextColor( 0xFF00FF00 );
				holder.date.setTextColor( 0xFF00FF00 );
				//holder.transcription.setTextColor( 0xFF00FF00 );
				break;
			case 2:
				holder.leftBy.setTextColor( 0xFF0000FF );
				holder.date.setTextColor( 0xFF0000FF );
				//holder.transcription.setTextColor( 0xFF0000FF );
				break;
			case 3:
				holder.leftBy.setTextColor( 0xFFFFFF00 );
				holder.date.setTextColor( 0xFFFFFF00 );
				//holder.transcription.setTextColor( 0xFFFFFF00 );
				break;
			case 4:
				holder.leftBy.setTextColor( 0xFF00FFFF );
				holder.date.setTextColor( 0xFF00FFFF );
				//holder.transcription.setTextColor( 0xFF00FFFF );
				break;
			default:
				holder.leftBy.setTextColor( 0xFFFFFFFF );
				holder.date.setTextColor( 0xFFFFFFFF );
				//holder.transcription.setTextColor( 0xFFFFFFFF );
				break;
			}*/
			
			return convertView;
		}

		private class ViewHolder {
			TextView leftBy;
			TextView date;
			//TextView transcription;
		}
	}
}