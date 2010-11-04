package com.interact.listen.android.voicemail;

import android.media.MediaPlayer;

import java.io.IOException;

public class VoicemailPlayer implements AudioController.Player
{
    private MediaPlayer mediaPlayer;
    private String audioPath;
    
    public VoicemailPlayer()
    {
        mediaPlayer = new MediaPlayer();
        audioPath = null;
    }

    public void reset()
    {
        mediaPlayer.reset();
    }
    
    public void prepare(String path) throws IOException {
        reset();
        audioPath = path;
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
        }
        catch (IOException e)
        {
            audioPath = null;
            throw e;
        }
    }
    
    @Override
    public void start()
    {
        if (audioPath != null)
        {
            mediaPlayer.start();
        }
    }

    @Override
    public boolean pause()
    {
        if (audioPath != null)
        {
            mediaPlayer.pause();
        }
        return true;
    }

    @Override
    public boolean seekTo(int pos)
    {
        if (audioPath != null)
        {
            mediaPlayer.seekTo(pos);
            return true;
        }
        return false;
    }

    @Override
    public int getDuration()
    {
        if (audioPath != null)
        {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition()
    {
        if (audioPath != null)
        {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getBufferPercentage()
    {
        return 100;
    }

    @Override
    public boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }

}
