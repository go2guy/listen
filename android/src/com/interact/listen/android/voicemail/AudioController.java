package com.interact.listen.android.voicemail;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

public class AudioController
{
    public interface Player
    {

        /**
         * Set the audio stream.  Can be called at any time.
         * @param audioStream
         */
        void setAudioStream(int audioStream);
        
        /**
         * Start the audio.
         */
        void start();
        
        /**
         * Pause the audio.
         * @return false if unable to pause
         */
        boolean pause();
        
        /**
         * Seek to a certain point in the audio
         * @param pos position in milliseconds
         * @return true if able to perform seek to the position
         */
        boolean seekTo(int pos);

        /**
         * @return length of audio in milliseconds
         */
        int getDuration();
        
        /**
         * @return current position in the audio in milliseconds
         */
        int getCurrentPosition();
        
        /**
         * @return percentage of audio ready for playing, [0,100]
         */
        int getBufferPercentage();

        /**
         * @return true if currently playing
         */
        boolean isPlaying();
    }

    private static final String TAG = Constants.TAG + "AudioControl";
    
    private static final int SHOW_PROGRESS = 1;

    private Player player;
    private View view;

    private ProgressBar progressBar;
    private TextView endTime;
    private TextView curTime;
    private ImageButton pauseButton;
    private ImageButton fastForwardButton;
    private ImageButton rewindButton;
    private ImageButton speakerButton;

    private boolean dragging;
    
    private StringBuilder formatBuilder;
    private Formatter formatter;

    private int audioStream;
    
    public AudioController()
    {
        player = null;
        view = null;
        audioStream = AudioManager.STREAM_VOICE_CALL;
    }

    public Player getPlayer()
    {
        return player;
    }
    public View getView()
    {
        return view;
    }

    public void setPlayer(Player p)
    {
        player = p;
        if(player != null)
        {
            p.setAudioStream(audioStream);
        }
        updatePausePlay();
    }
    
    public void onDestroy()
    {
        player = null;
        view = null;

        progressBar = null;
        endTime = null;
        curTime = null;
        pauseButton = null;
        fastForwardButton = null;
        rewindButton = null;
        speakerButton = null;
    }

    public void updateStream(int stream)
    {
        // we just support the two
        if(audioStream != stream)
        {
            doSpeakerToggle();
        }
    }
    
    public static View makeView(ViewGroup root)
    {
        LayoutInflater inflate = (LayoutInflater)root.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflate.inflate(R.layout.audio_player, root);
    }

    public void initializeController(View v)
    {
        view = v;
        
        pauseButton = (ImageButton)v.findViewById(R.id.pause);
        if(pauseButton != null)
        {
            pauseButton.requestFocus();
            pauseButton.setOnClickListener(pauseListener);
        }

        fastForwardButton = (ImageButton)v.findViewById(R.id.ffwd);
        if(fastForwardButton != null)
        {
            fastForwardButton.setOnClickListener(fastForwardListener);
        }

        rewindButton = (ImageButton)v.findViewById(R.id.rew);
        if(rewindButton != null)
        {
            rewindButton.setOnClickListener(rewindListener);
        }

        progressBar = (ProgressBar)v.findViewById(R.id.audiocontroller_progress);
        if(progressBar != null)
        {
            if(progressBar instanceof SeekBar)
            {
                SeekBar seeker = (SeekBar)progressBar;
                seeker.setOnSeekBarChangeListener(seekListener);
            }
            progressBar.setMax(1000);
        }
        
        speakerButton = (ImageButton)v.findViewById(R.id.speaker);
        if(speakerButton != null)
        {
            speakerButton.setOnClickListener(speakerListener);
        }

        endTime = (TextView)v.findViewById(R.id.time);
        curTime = (TextView)v.findViewById(R.id.time_current);

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        
        view.setOnKeyListener(keyListener);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        setProgress();
        updatePausePlay();
        if(player != null && player.isPlaying())
        {
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    }

    private void setEnabled(boolean enabled)
    {
        if(pauseButton != null && pauseButton.isEnabled() != enabled)
        {
            pauseButton.setEnabled(enabled);
        }
        if(fastForwardButton != null && fastForwardButton.isEnabled() != enabled)
        {
            fastForwardButton.setEnabled(enabled);
        }
        if(rewindButton != null && rewindButton.isEnabled() != enabled)
        {
            rewindButton.setEnabled(enabled);
        }
        if(progressBar != null && progressBar.isEnabled() != enabled)
        {
            progressBar.setEnabled(enabled);
        }
    }

    private void clearAnimation(boolean update)
    {
        Log.v(TAG, "stopping animations: " + update);
        if(pauseButton != null)
        {
            Animation anim = pauseButton.getAnimation();
            if(anim != null)
            {
                anim.cancel();
                pauseButton.clearAnimation();
                if(update)
                {
                    updatePausePlay();
                }
            }
            else if(pauseButton.getDrawable() instanceof AnimationDrawable)
            {
                AnimationDrawable drawable = (AnimationDrawable)pauseButton.getDrawable();
                drawable.stop();
            }
        }
    }

    public void setErrored()
    {
        Log.v(TAG, "setting player errored");
        setEnabled(false);

        if(pauseButton != null)
        {
            clearAnimation(false);
            pauseButton.setImageResource(R.drawable.ic_audio_error);
        }
    }

    public void setLoading()
    {
        Log.v(TAG, "setting player loading");
        setEnabled(false);
        if(pauseButton != null)
        {
            pauseButton.setImageResource(R.drawable.ic_loading);
        }
    }
    
    public void setPreparing()
    {
        Log.v(TAG, "setting player preparing");
        if(pauseButton != null)
        {
            pauseButton.setImageResource(R.drawable.ic_loading);
        }
    }

    public void onFocus(boolean focus)
    {
        Log.v(TAG, "Audio Controller focus: " + focus);
        if(pauseButton != null)
        {
            if(focus)
            {
                Animation anim = pauseButton.getAnimation();
                if(anim != null)
                {
                    Log.v(TAG, "starting animation");
                    anim.startNow();
                }
                else if(pauseButton.getDrawable() instanceof AnimationDrawable)
                {
                    Log.v(TAG, "starting drawable animation");
                    AnimationDrawable drawable = (AnimationDrawable)pauseButton.getDrawable();
                    drawable.start();
                }
            }
            else
            {
                clearAnimation(false);
            }
        }
    }
    
    public void setReady()
    {
        Log.v(TAG, "setting ready");
        setEnabled(true);
        clearAnimation(true);
    }
    
    public void setDisabled()
    {
        setEnabled(false);
        //clearAnimation(false);
    }
    
    public void update()
    {
        setProgress();
        updatePausePlay();
    }

    public void triggerPause()
    {
        if(player != null && player.isPlaying())
        {
            doPauseResume();
        }
    }

    public void triggerStart()
    {
        if(player != null && !player.isPlaying())
        {
            doPauseResume();
        }
    }
    
    private String stringForTime(int timeMs)
    {
        int rMs = timeMs;

        int r = timeMs % 1000;
        if(r >= 500)
        {
            rMs += 1000 - r;
        }
        
        int totalSeconds = rMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if(hours > 0)
        {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        }
        else
        {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void updateSecondaryProgress(int percent)
    {
        if(progressBar != null)
        {
            progressBar.setSecondaryProgress(percent * 10);
        }
    }
    
    private int setProgress()
    {
        if(player == null || dragging)
        {
            return 0;
        }
        int position = player.getCurrentPosition();
        int duration = player.getDuration();
        if(progressBar != null)
        {
            if(duration > 0)
            {
                long pos = 1000L * position / duration;
                progressBar.setProgress((int)pos);
            }
            int percent = player.getBufferPercentage();
            progressBar.setSecondaryProgress(percent * 10);
        }

        if(endTime != null)
        {
            endTime.setText(stringForTime(duration));
        }

        if(curTime != null)
        {
            curTime.setText(stringForTime(position));
        }
        
        return position;
    }

    private void updatePausePlay()
    {
        if(view != null && pauseButton != null)
        {
            if(player != null && player.isPlaying())
            {
                pauseButton.setImageResource(R.drawable.ic_audio_pause);
            }
            else
            {
                pauseButton.setImageResource(R.drawable.ic_audio_play);
            }
        }
    }

    private void doPauseResume()
    {
        if(player != null)
        {
            if(player.isPlaying())
            {
                player.pause();
            }
            else
            {
                player.start();
                handler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }
        updatePausePlay();
    }
    
    private void doSpeakerToggle()
    {
        if(audioStream == AudioManager.STREAM_VOICE_CALL)
        {
            speakerButton.setImageResource(R.drawable.ic_speaker_off);
            audioStream = AudioManager.STREAM_MUSIC;
        }
        else
        {
            speakerButton.setImageResource(R.drawable.ic_speaker_on);
            audioStream = AudioManager.STREAM_VOICE_CALL;
        }
        if(player != null)
        {
            player.setAudioStream(audioStream);
        }
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (player == null)
            {
                return;
            }
            switch(msg.what)
            {
                case SHOW_PROGRESS:
                    int pos = setProgress();
                    if(!dragging && player.isPlaying())
                    {
                        Message m = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(m, 1000 - (pos % 1000));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnKeyListener keyListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN &&
                (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE))
            {
                doPauseResume();
                if(pauseButton != null)
                {
                    pauseButton.requestFocus();
                }
                return true;
            }
            else if(keyCode == KeyEvent.KEYCODE_MEDIA_STOP)
            {
                if(player.isPlaying())
                {
                    player.pause();
                    updatePausePlay();
                }
                return true;
            }
            //else if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            //{
            //}
            return false;
        }
    };
    
    private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener()
    {
        public void onStartTrackingTouch(SeekBar bar)
        {
            dragging = true;
            handler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser)
        {
            if(!fromUser || player == null)
            {
                return; // updates from me are already covered
            }
            long duration = player.getDuration();
            int pos = (int)(duration * progress / 1000L);
            if (player.seekTo(pos))
            {
                if(curTime != null)
                {
                    curTime.setText(stringForTime(pos));
                }
            }
        }

        public void onStopTrackingTouch(SeekBar bar)
        {
            dragging = false;
            setProgress();
            updatePausePlay();
            handler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private View.OnClickListener speakerListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            doSpeakerToggle();
        }
    };

    private View.OnClickListener pauseListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            doPauseResume();
        }
    };

    private View.OnClickListener rewindListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(player != null)
            {
                int pos = player.getCurrentPosition() - 5000; // take off 5 seconds
                player.seekTo(Math.max(0, pos));
                setProgress();
            }
        }
    };

    private View.OnClickListener fastForwardListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(player != null && player.getDuration() > 0)
            {
                int pos = player.getCurrentPosition() + 5000; // add 5 seconds
                player.seekTo(Math.min(player.getDuration(), pos));
                setProgress();
            }
        }
    };

}
