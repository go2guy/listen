package com.interact.listen.android.voicemail;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ListenVoicemail extends Activity {
	
	Handler h;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final TextView textView = (TextView)findViewById(R.id.newVoicemails);
        
        this.h = new Handler() {
        	
        	@Override
        	public void handleMessage(Message msg) {
        		// process incoming messages here
        		switch(msg.what) {
        			case 0: {
        				textView.append((String)msg.obj);
        				break;
        			}
        		}
        		super.handleMessage(msg);
        	}
        };
        final Button button = (Button)findViewById(R.id.ButtonGo);
        button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			String numNewMessages = "0";
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
                    
            		textView.setText(numNewMessages);
            		
        		} catch(Exception e) {
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
}