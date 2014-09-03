package cz.mzk.kramerius.app.ui;

import java.io.IOException;

import android.app.Fragment;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;

public class PlayerFragment extends Fragment implements OnClickListener, OnCompletionListener, OnPreparedListener,
		OnBufferingUpdateListener, OnSeekBarChangeListener {

	private static final String TAG = PlayerFragment.class.getName();
	
	private SeekBar mSeekBar;
	private View mPlay;
	private View mPause;
	private View mPrevious;
	private View mNext;
	private MediaPlayer mMediaPlayer;
	private TextView mTimeView;
	private TextView mDurationView;

	private PlayerListener mCallback;

	private boolean mPlayAfterPrepared;
	private int mLastProgress;

	private Handler mHandler = new Handler();
	private Runnable mTimerTask = new Runnable() {
		public void run() {
			if (mMediaPlayer != null) {
				int duration = mMediaPlayer.getDuration();
				int position = mMediaPlayer.getCurrentPosition();
				int seek = 0;
				if (duration > 0) {
					seek = (int) ((position / (duration * 1.0)) * 100);
					mSeekBar.setProgress(seek);
					mTimeView.setText(timerToString(position));
				}
				mHandler.removeCallbacks(mTimerTask);
				mHandler.postDelayed(mTimerTask, 1000);
			}
		};
	};

	public void setCallback(PlayerListener callback) {
		mCallback = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_player, container, false);
		mPlay = view.findViewById(R.id.player_play);
		mPause = view.findViewById(R.id.player_pause);
		mPrevious = view.findViewById(R.id.player_previous);
		mNext = view.findViewById(R.id.player_next);
		mPlay.setOnClickListener(this);
		mPause.setOnClickListener(this);
		mNext.setOnClickListener(this);
		mPrevious.setOnClickListener(this);
		mPause.setVisibility(View.GONE);
		mPlay.setVisibility(View.VISIBLE);
		mTimeView = (TextView) view.findViewById(R.id.player_time);
		mDurationView = (TextView) view.findViewById(R.id.player_duration);
		mSeekBar = (SeekBar) view.findViewById(R.id.player_seekBar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setMax(100);

		return view;
	}

	@Override
	public void onPrepared(MediaPlayer mediaplayer) {
		if (!mMediaPlayer.isPlaying()) {
			mPlay.setVisibility(View.VISIBLE);
			mTimeView.setVisibility(View.VISIBLE);
			mTimeView.setText(timerToString(0));
			mDurationView.setVisibility(View.VISIBLE);
			mDurationView.setText(timerToString(mMediaPlayer.getDuration()));
			if(mPlayAfterPrepared) {
				onPlayButtonClicked();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		Log.d(TAG, "PLAYER:onComplete"  + mediaPlayer.isPlaying() + ", " + mediaPlayer.getCurrentPosition());
		stop();
		if (mCallback != null && mediaPlayer.getCurrentPosition() > 0) {
			mCallback.onPlayerComplete();
		}
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		mSeekBar.setSecondaryProgress(percent);
	}

	@Override
	public void onClick(View v) {
		if (v == mPlay) {
			onPlayButtonClicked();
		} else if (v == mPause) {
			onPauseButtonClicked();
		} else if (v == mPrevious) {
			onPreviousButtonClicked();
		} else if (v == mNext) {
			onNextButtonClicked();
		}
	}

	private void stop() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
		mDurationView.setText("");
		mTimeView.setText("");
		mSeekBar.setProgress(0);
	}

	public void play(String link, boolean playAfterPrepared) {
		Log.d(TAG, "PLAYER:play");
		stop();
		mPlayAfterPrepared = playAfterPrepared;
		mMediaPlayer.reset();
		Log.d(TAG, "PLAYER:play-afterReset");
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mMediaPlayer.setDataSource(link);
			// mediaPlayer.prepare(); // might take long! (for buffering, etc)
			// //@@
			mMediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block///
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onPlayButtonClicked() {
		play();
		if (mCallback != null) {
			mCallback.onPlayerPlay();
		}

	}

	private void onPauseButtonClicked() {
		pause();
		if (mCallback != null) {
			mCallback.onPlayerPause();
		}
	}
	
	public void pause() {
		if (!mMediaPlayer.isPlaying()) {
			return;
		}
		mMediaPlayer.pause();
		mPlay.setVisibility(View.VISIBLE);
		mPause.setVisibility(View.GONE);
		mHandler.removeCallbacks(mTimerTask);		
	}
	
	public void play() {
		if (mMediaPlayer.isPlaying()) {
			return;
		}
		mMediaPlayer.start();
		mPlay.setVisibility(View.GONE);
		mPause.setVisibility(View.VISIBLE);
		mHandler.postDelayed(mTimerTask, 1000);		
	}

	private void onPreviousButtonClicked() {
		if (mCallback != null) {
			mCallback.onPlayerPrevious();
		}
	}

	private void onNextButtonClicked() {
		if (mCallback != null) {
			mCallback.onPlayerNext();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mLastProgress = progress;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (mMediaPlayer == null) {
			return;
		}
		int secondaryPosition = seekBar.getSecondaryProgress();
		if (mLastProgress > secondaryPosition) {
			seekBar.setProgress(secondaryPosition);
		}
		int duration = mMediaPlayer.getDuration();
		int seek = duration * mLastProgress / 100;
		mMediaPlayer.seekTo(seek);
	}

	public static String timerToString(int time) {
		int sec = time / 1000;
		int days = sec / (60 * 60 * 24);
		int timeHours = sec % (60 * 60 * 24);
		int hours = timeHours / (60 * 60);
		int timeMinutes = timeHours % (60 * 60);
		int minutes = timeMinutes / 60;
		int seconds = timeMinutes % (60);
		String timeString = "";
		if (days > 0) {
			timeString += days + " days, ";
		}
		if (hours > 0) {
			timeString += addLeadingZeros(hours) + ":";
		}
		timeString += addLeadingZeros(minutes) + ":" + addLeadingZeros(seconds);
		return timeString;
	}

	private static String addLeadingZeros(int value) {
		if (value < 10) {
			return "0" + value;
		} else {
			return "" + value;
		}
	}

	public interface PlayerListener {
		public void onPlayerPlay();

		public void onPlayerPause();

		public void onPlayerNext();

		public void onPlayerPrevious();
		
		public void onPlayerComplete();
	}

}