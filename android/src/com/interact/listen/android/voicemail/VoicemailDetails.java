package com.interact.listen.android.voicemail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.interact.listen.android.voicemail.controller.ControllerAdapter;
import com.interact.listen.android.voicemail.controller.DefaultController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VoicemailDetails extends Activity
{
    private static final String TAG = "ListenVoicemailDetails";
    private TextView mLeftBy;
    private TextView mDate;
    private TextView mTranscription;
    private long mVoicemailId;
    private int mPosition;
    private String pathToAudioFile;
    private ControllerAdapter controller = new ControllerAdapter(new DefaultController());
    ProgressDialog progressDialog = null;

    private ListenVoicemailServiceBinder serviceBinder = new ListenVoicemailServiceBinder(this);

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voicemail_details);

        /*
         * if(savedInstanceState == null) { // We just want to go back to the list of voicemails setResult(RESULT_OK);
         * finish(); }
         */

        Bundle extras = getIntent().getExtras();
        mVoicemailId = extras.getLong("id");
        String leftBy = extras.getString("leftBy");
        String date = extras.getString("date");
        String transcription = extras.getString("transcription");
        mPosition = extras.getInt("position");
        
        new DownloadVoicemail().execute(mVoicemailId);

        mLeftBy = (TextView)findViewById(R.id.detailLeftBy);
        mDate = (TextView)findViewById(R.id.detailDate);
        mTranscription = (TextView)findViewById(R.id.detailTranscription);

        mLeftBy.setText(leftBy);
        mDate.setText(date);
        mTranscription.setText(transcription);

        serviceBinder.bind(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    serviceBinder.getService().markVoicemailOld(mVoicemailId);
                }
                catch(RemoteException e)
                {
                    Log.e(TAG, "Error marking voicemail [" + mVoicemailId + "] as old", e);
                }
            }
        });

        Button deleteButton = (Button)findViewById(R.id.delete);
        deleteButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    serviceBinder.getService().deleteVoicemail(mVoicemailId);
                }
                catch(RemoteException e)
                {
                    Log.e(TAG, "Error deleting voicemail [" + mVoicemailId + "]", e);
                }
                setOkResult(true);
                finish();
            }
        });
        
        Button playButton = (Button)findViewById(R.id.play);
        playButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
            	try
            	{
            		MediaPlayer mediaPlayer = new MediaPlayer();
            		mediaPlayer.setDataSource(pathToAudioFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
            	}
            	catch(IOException e)
            	{
            		Log.e(TAG, "Error trying to play audio file.", e);
            		Toast.makeText(VoicemailDetails.this, "Error trying to play audio", Toast.LENGTH_SHORT).show();
            	}
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        serviceBinder.unbind();
    }

    private void setOkResult(Boolean deleted)
    {
        Bundle bundle = new Bundle();
        bundle.putInt("position", mPosition);

        Intent intent = new Intent();
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
    }
    
    private class DownloadVoicemail extends AsyncTask<Long, Void, String>
    {
    	@Override 
    	protected void onPreExecute()
    	{
    		Button playButton = (Button)findViewById(R.id.play);
    		playButton.setVisibility(View.INVISIBLE);
    		progressDialog = ProgressDialog.show(VoicemailDetails.this, "", "Downloading Voicemail...");
            progressDialog.getWindow().setGravity(Gravity.BOTTOM);;
    		
    	}
    	
        @Override
        protected String doInBackground(Long... ids)
        {
            Log.v(TAG, "DownloadVoicemail.doInBackground()");
            return controller.downloadVoicemailToTempFile(VoicemailDetails.this, ids[0]);
        }

        @Override
        protected void onPostExecute(String path)
        {
            Log.v(TAG, "DownloadVoicemail.onPostExecute()");
            progressDialog.dismiss();
            pathToAudioFile = path;
            
            if(!pathToAudioFile.equals(""))
            {
            	Button playButton = (Button)findViewById(R.id.play);
            	playButton.setVisibility(View.VISIBLE);
            }
            else
            {
            	Toast.makeText(VoicemailDetails.this, "There was an Error downloading the voicemail", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
