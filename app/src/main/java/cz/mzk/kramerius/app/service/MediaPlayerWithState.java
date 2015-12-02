package cz.mzk.kramerius.app.service;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import java.io.IOException;

import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Track.AudioFormat;

public class MediaPlayerWithState {

    // private static final Logger LOGGER = Logger.getLogger(MediaPlayerWithState.class.getSimpleName());

    public enum State {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PAUSED, PLAYBACK_COMPLETE, END, ERROR;
    }

    private MediaPlayer mPlayer;
    private State mState;
    private String mTrackPid;
    private Integer mDuration = null;

    private void setState(State state) {
        // LOGGER.info("state: " + state);
        this.mState = state;
    }

    public MediaPlayerWithState() {
        synchronized (this) {
            mPlayer = new MediaPlayer();
            setOnPreparedListener(null);
            setOnCompletionListener(null);
            setOnErrorListener(null);
            setState(State.IDLE);
        }
    }

    public synchronized void reset() {
        // LOGGER.fine("resetting");
        mPlayer.reset();
        mTrackPid = null;
        mDuration = null;
        setState(State.IDLE);
    }

    public synchronized void release() {
        // LOGGER.fine("releasing");
        mPlayer.release();
        mTrackPid = null;
        mDuration = null;
        setState(State.END);
    }

    public synchronized void setDataSource(Context context, String trackPid) throws IllegalArgumentException,
            SecurityException, IllegalStateException, IOException {
        // LOGGER.fine("setting data source");
        this.mTrackPid = trackPid;
        setAvailableDatasource(context);
    }

    private void setAvailableDatasource(Context context) throws IllegalArgumentException, SecurityException,
            IllegalStateException, IOException {
        if (mTrackPid != null) {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(K5Api.getAudioStreamPath(context, mTrackPid, AudioFormat.MP3));
            } catch (IOException e) {
                try {
                    // getDuration() allways returns 0 for OGG, but perhaps it is in data
                    mPlayer.setDataSource(K5Api.getAudioStreamPath(context, mTrackPid, AudioFormat.OGG));
                } catch (IOException e1) {
                    // last resort
                    mPlayer.setDataSource(K5Api.getAudioStreamPath(context, mTrackPid, AudioFormat.WAV));
                }
            }
            setState(State.INITIALIZED);
        }
    }

    public void setOnPreparedListener(final OnPreparedListener listener) {
        mPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                synchronized (MediaPlayerWithState.this) {
                    // duration = player.getDuration();
                    mDuration = mp.getDuration();
                    setState(State.PREPARED);
                }
                if (listener != null) {
                    listener.onPrepared(mp);
                }
            }
        });
    }

    public void setOnErrorListener(final OnErrorListener listener) {
        mPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                synchronized (MediaPlayerWithState.this) {
                    mDuration = null;
                    mTrackPid = null;
                    setState(State.ERROR);
                }
                if (listener != null) {
                    return listener.onError(mp, what, extra);
                } else {
                    return false;
                }
            }
        });
    }

    ;

    public void setOnCompletionListener(final OnCompletionListener listener) {
        mPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                synchronized (MediaPlayerWithState.this) {
                    setState(State.PLAYBACK_COMPLETE);
                }
                if (listener != null) {
                    listener.onCompletion(mp);
                }
            }
        });
    }

    ;

    public synchronized void prepareAsync() throws IllegalStateException {
        // LOGGER.fine("preparing async");
        mPlayer.prepareAsync();
        setState(State.PREPARING);
    }

    public synchronized void start() throws IllegalStateException {
        // LOGGER.fine("starting");
        mPlayer.start();
        setState(State.STARTED);
    }

    public synchronized void pause() throws IllegalStateException {
        // LOGGER.fine("pausing");
        mPlayer.pause();
        setState(State.PAUSED);
    }

    public synchronized void stop() throws IllegalStateException {
        // LOGGER.fine("stopping");
        mPlayer.stop();
        setState(State.STOPPED);
    }

    public synchronized State getState() {
        return mState;
    }

    public String getTrackPid() {
        return mTrackPid;
    }

    public synchronized boolean canStop() {
        return mState != State.IDLE && mState != State.INITIALIZED && mState != State.ERROR;
    }

    public synchronized boolean canPause() {
        return mState == State.STARTED || mState == State.PAUSED || mState == State.PLAYBACK_COMPLETE;
    }

    public synchronized boolean canStart() {
        return mState == State.PREPARED || mState == State.STARTED || mState == State.PAUSED
                || mState == State.PLAYBACK_COMPLETE;
    }

    public synchronized boolean canPrepare() {
        return mState == State.INITIALIZED || mState == State.STOPPED;
    }

    public synchronized Integer getCurrentPosition() {
        if (mState == State.IDLE || mState == State.INITIALIZED || mState == State.PREPARED || mState == State.STARTED
                || mState == State.PAUSED || mState == State.STOPPED || mState == State.PLAYBACK_COMPLETE) {
            return mPlayer.getCurrentPosition();
        } else {
            return null;
        }
    }

    /**
     * Reasons for such implementation is thta MediaPlayer.getDuration() may crash (with error (-38,0)) eve if it is started.
     * http://stackoverflow.com/questions/5710922/android-attempt-to-call-getduration-without-a-valid-mediaplayer Similar error
     * happens in Nexus 7 2012 (Android 4.4.4) with OGG streams
     *
     * @return
     */
    public synchronized Integer getDuration() {
        // LOGGER.info("getDuration, state: " + state);
        // if (state == State.PREPARED || state == State.STARTED || state == State.PAUSED || state == State.STOPPED
        // || state == State.PLAYBACK_COMPLETE) {
        // return player.getDuration();
        // } else {
        // return null;
        // }
        return mDuration;
    }

    public synchronized void seekTo(int timeMs) {
        // LOGGER.fine("seeking");
        if (mState == State.PREPARED || mState == State.STARTED || mState == State.PAUSED
                || mState == State.PLAYBACK_COMPLETE) {
            mPlayer.seekTo(timeMs);
        }
    }

}
