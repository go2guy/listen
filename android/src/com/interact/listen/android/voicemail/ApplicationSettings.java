package com.interact.listen.android.voicemail;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ApplicationSettings extends Activity
{
    private static final String TAG = "ListenVoicemailDetails";
    private TextView mLeftBy;
    private TextView mDate;
    private TextView mTranscription;
    private long mVoicemailId;
    private ArrayList<Voicemail> mVoicemails;
    private int mPosition;
    private String UPDATE_ACTION_STRING = "com.interact.listen.android.voicemail.UPDATE_VOICEMAILS";
    
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received broadcast");
            abortBroadcast();
            //do nothing, we just don't want the notification to appear
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_settings);
        
        Button saveButton = (Button) findViewById(R.id.settingsSave);
        Button cancelButton = (Button) findViewById(R.id.settingsCancel);
        
        saveButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                try
                {
                    Context context = getApplicationContext();
                    CharSequence text = "You clicked save!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast.makeText(context, text, duration).show();
                    /*new MarkVoicemailRead().execute(mVoicemailId);
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", mPosition);
                    bundle.putBoolean("deleted", false);
                    
                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);*/
                    
                    setResult(RESULT_OK/*, mIntent*/);
                    finish();
                } catch(Exception e) {
                    Log.e("TONY", "Exception marking voicemail read", e);
                }
            }
        });
    }
}