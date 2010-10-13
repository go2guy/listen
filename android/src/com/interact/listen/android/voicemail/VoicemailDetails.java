package com.interact.listen.android.voicemail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
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
}
