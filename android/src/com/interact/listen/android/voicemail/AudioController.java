package com.interact.listen.android.voicemail;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.interact.listen.android.voicemail.R;

import java.util.Formatter;
import java.util.Locale;

public class AudioController
{
    public interface Player
    {
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
         * 
         * @return length of audio in milliseconds
         */
        int getDuration();
        
        /**
         * 
         * @return current position in the audio in milliseconds
         */
        int getCurrentPosition();
        
        /**
         * 
         * @return percentage of audio ready for playing, [0,100]
         */
        int getBufferPercentage();

        /**
         * 
         * @return true if currently playing
         */
        boolean isPlaying();
    }

    private static final int SHOW_PROGRESS = 1;

    private Player player;
    private View view;

    private ProgressBar progressBar;
    private TextView endTime;
    private TextView curTime;
    private ImageButton pauseButton;
    private ImageButton fastForwardButton;
    private ImageButton rewindButton;

    private boolean dragging;
    
    private StringBuilder formatBuilder;
    private Formatter formatter;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case SHOW_PROGRESS:
                    int pos = setProgress();
                    if(!dragging && player != null && player.isPlaying())
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
            long duration = player.getDuration();
            int pos = (int)((long)(duration * progress) / 1000L);
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
            int pos = player.getCurrentPosition();
            pos -= 5000; // 5 seconds
            player.seekTo(pos);
            setProgress();
        }
    };

    private View.OnClickListener fastForwardListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            int pos = player.getCurrentPosition();
            pos += 15000; // 15 seconds
            player.seekTo(pos);
            setProgress();
        }
    };

    public AudioController()
    {
        player = null;
        view = null;
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
        updatePausePlay();
    }
    
    public static View makeView(ViewGroup root)
    {
        LayoutInflater inflate = (LayoutInflater)root.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflate.inflate(R.layout.audio_controller, root);
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

        endTime = (TextView)v.findViewById(R.id.time);
        curTime = (TextView)v.findViewById(R.id.time_current);

        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        
        view.setOnKeyListener(keyListener);

        setProgress();
        updatePausePlay();
        handler.sendEmptyMessage(SHOW_PROGRESS);
    }
    
    public void setEnabled(boolean enabled)
    {
        if(pauseButton != null)
        {
            pauseButton.setEnabled(enabled);
        }
        if(fastForwardButton != null)
        {
            fastForwardButton.setEnabled(enabled);
        }
        if(rewindButton != null)
        {
            rewindButton.setEnabled(enabled);
        }
        if(progressBar != null)
        {
            progressBar.setEnabled(enabled);
        }
    }

    private String stringForTime(int timeMs)
    {
        int totalSeconds = timeMs / 1000;
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
        if(player.isPlaying())
        {
            player.pause();
        }
        else
        {
            player.start();
        }
        updatePausePlay();
    }

}
