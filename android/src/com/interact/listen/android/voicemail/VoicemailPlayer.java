package com.interact.listen.android.voicemail;

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
    private int audioStream;
    private int targetAudioStream;
    
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
        audioStream = AudioManager.STREAM_VOICE_CALL;
        targetAudioStream = AudioManager.STREAM_VOICE_CALL;
        
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
            audioController.updateSecondaryProgress(currentBufferPercentage);
        }
    }

    public void setLoading()
    {
        if(audioController != null)
        {
            audioController.setLoading();
        }
    }
    
    public void setErrored()
    {
        if(audioController != null)
        {
            audioController.setErrored();
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

    public void triggerPause()
    {
        if(audioController != null)
        {
            audioController.triggerPause();
        }
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
        int cp = seekWhenPrepared;
        if(isInPlaybackState())
        {
            cp = mediaPlayer.getCurrentPosition();
            Log.v(TAG, "getCurrentPosition() = " + cp);
        }
        else
        {
            Log.v(TAG, "getCurrentPosition() = seek when prepared: " + cp);
        }
        if(durationMS >= 0 && cp > durationMS)
        {
            Log.v(TAG, "playback position is over duration " + durationMS);
            cp = durationMS;
        }
        return cp;
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

    @Override
    public void setAudioStream(int stream)
    {
        targetAudioStream = stream;
        updateAudioStream();
    }
    
    private boolean updateAudioStream()
    {
        if(targetAudioStream == audioStream)
        {
            return false;
        }
        
        switch(currentState)
        {
            case PREPARING:
                return false;
            case ERROR:
            case IDLE:
                audioStream = targetAudioStream;
                return false;
            case PREPARED:
            case PLAYING:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                break;
            default:
                return false;
        }
        
        if(mediaPlayer == null)
        {
            return false;
        }
        
        seekWhenPrepared = getCurrentPosition();

        if(audioController != null)
        {
            audioController.setPreparing();
        }
        
        currentState = State.REPREPARING;

        mediaPlayer.reset();

        Log.v(TAG, "re-preparing data source " + uri + " stream " + targetAudioStream);
        try
        {
            mediaPlayer.setDataSource(context, uri);
        }
        catch(IllegalArgumentException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return false;
        }
        catch(IOException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return false;
        }
        
        audioStream = targetAudioStream;
        mediaPlayer.setAudioStreamType(audioStream);

        currentState = State.PREPARING;
        mediaPlayer.prepareAsync();

        return true;
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
            
            Log.v(TAG, "setting data source " + uri + " stream " + targetAudioStream);
            mediaPlayer.setDataSource(context, uri);

            audioStream = targetAudioStream;
            mediaPlayer.setAudioStreamType(audioStream);

            Log.v(TAG, "preparing media " + uri);
            mediaPlayer.prepareAsync();
            currentState = State.PREPARING;
            attachAudioController();
        }
        catch(IOException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            if(audioController != null)
            {
                audioController.setErrored();
            }
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
        catch(IllegalArgumentException e)
        {
            Log.w(TAG, "Unable to open content: " + uri, e);
            if(audioController != null)
            {
                audioController.setErrored();
            }
            currentState = State.ERROR;
            targetState = State.ERROR;
            errorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }
    
    private void attachAudioController()
    {
        if(audioController != null)
        {
            audioController.setPlayer(this);
            if(isInPlaybackState())
            {
                audioController.setReady();
            }
            else if(currentState == State.PREPARING || currentState == State.REPREPARING)
            {
                audioController.setPreparing();
            }
            else
            {
                audioController.setDisabled();
            }
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
                    audioController.setDisabled();
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
        ERROR, IDLE, PREPARING, REPREPARING, PREPARED, PLAYING, PAUSED, PLAYBACK_COMPLETED;
        
        public boolean isPlayable()
        {
            return this != ERROR && this != IDLE && this != PREPARING && this != REPREPARING;
        }
    }

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener()
    {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            Log.i(TAG, "Player prepared: " + uri);
            currentState = State.PREPARED;

            if(updateAudioStream())
            {
                return;
            }
            
            if(onPreparedListener != null)
            {
                onPreparedListener.onPrepared(mediaPlayer);
            }

            int seekToPosition = seekWhenPrepared;
            seekWhenPrepared = 0;
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
                audioController.setReady();
                audioController.update();
            }
        }
    };

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mp)
        {
            if (currentState == State.REPREPARING)
            {
                Log.v(TAG, "completed in re-preparing state");
                return;
            }
            Log.i(TAG, "Media player done playing audio at " + mediaPlayer.getDuration() + " (" + durationMS +
                  ") / " + mediaPlayer.getCurrentPosition());
            
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
                audioController.setErrored();
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
            if (currentState == State.REPREPARING)
            {
                Log.v(TAG, "buffer update in re-preparing state");
                return;
            }

            Log.i(TAG, "Buffer update to " + percent);
            currentBufferPercentage = percent;
            if(audioController != null)
            {
                audioController.update();
            }
        }
    };

}
