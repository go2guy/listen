package com.interact.listen.android.voicemail;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class VoicemailPlayer implements AudioController.Player
{
    private static final String TAG = Constants.TAG + "Player";
    
    private MediaPlayer mediaPlayer;
    private Uri uri;
    private int durationMS;
    private State currentState;
    private State targetState;
    private int currentBufferPercentage;
    private int seekWhenPrepared;
    
    private AudioController audioController;
    
    private MediaPlayer.OnPreparedListener onPreparedListener;
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer.OnErrorListener onErrorListener;

    private Context context;
    
    public VoicemailPlayer()
    {
        mediaPlayer = null;
        uri = null;
        durationMS = 0;
        currentState = State.IDLE;
        targetState = State.IDLE;
        currentBufferPercentage = 0;
        seekWhenPrepared = 0;
        
        audioController = null;
        
        onPreparedListener = null;
        onCompletionListener = null;
        onErrorListener = null;
        
        context = null;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
    {
        onPreparedListener = l;
    }
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l)
    {
        onCompletionListener = l;
    }
    public void setOnErrorListener(MediaPlayer.OnErrorListener l)
    {
        onErrorListener = l;
    }

    public void setAudioController(AudioController controller)
    {
        audioController = controller;
        attachAudioController();
    }

    public void updateBufferPercentage(int percent)
    {
        Log.v(TAG, "manual buffer update to " + percent);
        currentBufferPercentage = percent;
        if(audioController != null)
        {
            audioController.update();
        }
    }

    public void setLoading()
    {
        if(audioController != null)
        {
            audioController.setLoading();
        }
    }
    
    public void setControllerEnabled(boolean enabled)
    {
        if(audioController != null)
        {
            audioController.setEnabled(enabled);
        }
    }
    
    public void setAudioFile(Context ctx, File file)
    {
        setAudioURI(ctx, Uri.fromFile(file));
    }

    public void setAudioURI(Context ctx, Uri u)
    {
        uri = u;
        seekWhenPrepared = 0;
        openAudio(ctx);
    }
    
    public boolean isAudioSet()
    {
        return uri != null && mediaPlayer != null;
    }

    public void stopPlayback()
    {
        if(mediaPlayer != null)
        {
            try
            {
                mediaPlayer.stop();
            }
            catch (IllegalStateException e)
            {
                Log.i(TAG, "media player not in legal state to stop");
            }
        }
        release(true);
    }

    public AudioController getController()
    {
        return audioController;
    }
    
    @Override
    public void start()
    {
        if(isInPlaybackState() && currentState != State.PLAYING)
        {
            mediaPlayer.start();
            currentState = State.PLAYING;
        }
        targetState = State.PLAYING;
    }

    @Override
    public boolean pause()
    {
        if(isInPlaybackState())
        {
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                currentState = State.PAUSED;
            }
        }
        targetState = State.PAUSED;
        return true;
    }

    @Override
    public int getDuration()
    {
        if(isInPlaybackState())
        {
            if(durationMS < 0)
            {
                durationMS = mediaPlayer.getDuration();
                Log.i(TAG, "Caching duration " + durationMS);
            }
            return durationMS;
        }
        durationMS = -1;
        return 0;
    }

    @Override
    public int getCurrentPosition()
    {
        return isInPlaybackState() ? mediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public boolean seekTo(int msec)
    {
        if(isInPlaybackState())
        {
            Log.i(TAG, "Seeking to " + msec);
            mediaPlayer.seekTo(msec);
            seekWhenPrepared = 0;
        }
        else
        {
            Log.i(TAG, "Will seek to " + msec);
            seekWhenPrepared = msec;
        }
        return true;
    }

    @Override
    public boolean isPlaying()
    {
        return isInPlaybackState() && mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage()
    {
        return mediaPlayer != null ? currentBufferPercentage : 0;
    }

    private void openAudio(Context ctx)
    {
        context = ctx;
        if (context == null && audioController != null && audioController.getView() != null)
        {
            context = audioController.getView().getContext();
        }
        
        if(uri == null)
        {
            return;
        }
        Log.i(TAG, "Opening audio " + uri);

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        context.sendBroadcast(i);

        release(false);
        
        try
        {
            Log.v(TAG, "creating new media player");
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(preparedListener);
            durationMS = -1;
            mediaPlayer.setOnCompletionListener(completionListener);
            mediaPlayer.setOnErrorListener(errorListener);
            mediaPlayer.setOnBufferingUpdateListener(bufferUpdateListener);
            currentBufferPercentage = 0;
            
            if(ContentResolver.SCHEME_FILE.equals(uri.getScheme()))
            {
                Log.v(TAG, "setting file content source " + uri.getPath());
                mediaPlayer.setDataSource(uri.getPath());
            }
            else
            {
                Log.v(TAG, "setting data source " + uri);
                mediaPlayer.setDataSource(context, uri);
            }
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            Log.v(TAG, "preparing media " + uri);
            mediaPlayer.prepareAsync();
            currentState = State.PREPARING;
            attachAudioController();
        }
        catch(IOException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        catch(IllegalArgumentException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }
    
    private void attachAudioController()
    {
        if(mediaPlayer != null && audioController != null)
        {
            audioController.setPlayer(this);
            audioController.setEnabled(isInPlaybackState());
        }
    }

    private void release(boolean resetTargetState)
    {
        if(mediaPlayer != null)
        {
            Log.v(TAG, "reseting media player");
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            currentState = State.IDLE;
            if(resetTargetState)
            {
                targetState = State.IDLE;
                if(audioController != null)
                {
                    audioController.setEnabled(false);
                }
            }
        }
    }

    private boolean isInPlaybackState()
    {
        return mediaPlayer != null && currentState.isPlayable();
    }

    private enum State
    {
        ERROR, IDLE, PREPARING, PREPARED, PLAYING, PAUSED, PLAYBACK_COMPLETED;
        
        public boolean isPlayable()
        {
            return this != ERROR && this != IDLE && this != PREPARING;
        }
    }

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener()
    {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            Log.i(TAG, "Player prepared: " + uri);
            currentState = State.PREPARED;

            if(onPreparedListener != null)
            {
                onPreparedListener.onPrepared(mediaPlayer);
            }

            int seekToPosition = seekWhenPrepared;
            if(seekToPosition != 0)
            {
                seekTo(seekToPosition);
            }

            if(targetState == State.PLAYING)
            {
                Log.i(TAG, "Player starting right up");
                start();
            }
            if(audioController != null)
            {
                audioController.setEnabled(true);
                audioController.update();
            }
        }
    };

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            Log.i(TAG, "Media player done playing audio at " + mediaPlayer.getDuration() + " of " + durationMS);
            currentState = State.PLAYBACK_COMPLETED;
            targetState = State.PLAYBACK_COMPLETED;

            if(audioController != null)
            {
                audioController.update();
            }
            if(onCompletionListener != null)
            {
                onCompletionListener.onCompletion(mediaPlayer);
            }
        }
    };

    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener()
    {
        @Override
        public boolean onError(MediaPlayer mp, int frameworkErr, int implErr)
        {
            Log.e(TAG, "Error: " + frameworkErr + "," + implErr);
            currentState = State.ERROR;
            targetState = State.ERROR;
            if(audioController != null)
            {
                audioController.setEnabled(false);
            }

            if(onErrorListener != null)
            {
                if(onErrorListener.onError(mediaPlayer, frameworkErr, implErr))
                {
                    return true;
                }
            }

            Toast.makeText(context, "Unable to play voicemail", Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener bufferUpdateListener = new MediaPlayer.OnBufferingUpdateListener()
    {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {
            Log.i(TAG, "Buffer update to " + percent);
            currentBufferPercentage = percent;
            if(audioController != null)
            {
                audioController.update();
            }
        }
    };

}
