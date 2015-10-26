package cz.mzk.kramerius.app.ui;

import java.util.logging.Logger;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.service.MediaPlayerService;
import cz.mzk.kramerius.app.service.MediaPlayerWithState.State;

public class PlayerFragment extends Fragment implements OnClickListener, OnSeekBarChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PlayerFragment.class.getSimpleName());

    private View mRootView;
    private ProgressBar mProgressBarLoading;
    private SeekBar mSeekBarKnownLength;
    private SeekBar mSeekBarUnknownLength;
    private View mBtnResume;
    private View mBtnPause;
    private View mBtnPrevious;
    private View mBtnNext;
    private TextView mCurrentTime;
    private TextView mTotalTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        mRootView = view;
        mBtnResume = view.findViewById(R.id.resume);
        mBtnPause = view.findViewById(R.id.pause);
        mBtnPrevious = view.findViewById(R.id.previous);
        mBtnNext = view.findViewById(R.id.next);
        mBtnResume.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnPrevious.setOnClickListener(this);
        mCurrentTime = (TextView) view.findViewById(R.id.current_time);
        mTotalTime = (TextView) view.findViewById(R.id.duration);
        mProgressBarLoading = (ProgressBar) view.findViewById(R.id.progressBar_loading);
        mSeekBarKnownLength = (SeekBar) view.findViewById(R.id.seekBar_known_length);
        mSeekBarKnownLength.setOnSeekBarChangeListener(this);
        mSeekBarUnknownLength = (SeekBar) view.findViewById(R.id.seekBar_unknown_length);
        mSeekBarUnknownLength.setOnSeekBarChangeListener(this);
        return view;
    }

    public void update(boolean enabled, State state, Integer currentMillis, Integer totalMillis) {
        mRootView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (enabled) {
            updatePrimaryButtonVisibility(state);
            if (currentMillis == null || currentMillis == 0) {
                mCurrentTime.setText("--:--");
                mTotalTime.setText("--:--");
                mProgressBarLoading.setVisibility(View.VISIBLE);
                mSeekBarUnknownLength.setVisibility(View.INVISIBLE);
                mSeekBarKnownLength.setVisibility(View.INVISIBLE);
            } else {// playing or paused
                mCurrentTime.setText(timeToString(currentMillis));
                mProgressBarLoading.setVisibility(View.INVISIBLE);
                if (totalMillis == null || totalMillis == 0) {// unknown length
                    mSeekBarKnownLength.setVisibility(View.INVISIBLE);
                    mSeekBarUnknownLength.setVisibility(View.VISIBLE);
                    mSeekBarUnknownLength.setMax(currentMillis);
                    mSeekBarUnknownLength.setProgress(currentMillis);
                    mTotalTime.setText("--:--");
                } else {// known length
                    mSeekBarUnknownLength.setVisibility(View.INVISIBLE);
                    mSeekBarKnownLength.setVisibility(View.VISIBLE);
                    mTotalTime.setText(timeToString(totalMillis));
                    mSeekBarKnownLength.setMax(totalMillis);
                    mSeekBarKnownLength.setProgress(currentMillis);
                }
            }
        }
    }

    private void updatePrimaryButtonVisibility(State state) {
        int pauseVisibility = View.INVISIBLE;
        int playVisibility = View.INVISIBLE;
        if (state != null) {
            if (state == State.STARTED) {
                pauseVisibility = View.VISIBLE;
            } else if (state == State.PAUSED) {
                playVisibility = View.VISIBLE;
            }
        }
        mBtnResume.setVisibility(playVisibility);
        mBtnPause.setVisibility(pauseVisibility);
    }

    @Override
    public void onClick(View v) {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, MediaPlayerService.class);
        if (v == mBtnResume) {
            intent.setAction(MediaPlayerService.ACTION_RESUME);
        } else if (v == mBtnPause) {
            intent.setAction(MediaPlayerService.ACTION_PAUSE);
        } else if (v == mBtnPrevious) {
            intent.setAction(MediaPlayerService.ACTION_PREVIOUS);
        } else if (v == mBtnNext) {
            intent.setAction(MediaPlayerService.ACTION_NEXT);
        } else {
            return;
        }
        PendingIntent pendingIntent = PendingIntent.getService(activity, MediaPlayerService.SERVICE_PI_REQ_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            LOGGER.warning("intent canceled: " + e.getMessage());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // nothing
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // nothing
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mSeekBarKnownLength) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent(activity, MediaPlayerService.class);
                intent.setAction(MediaPlayerService.ACTION_REWIND);
                int progress = seekBar.getProgress();
                Bundle bundle = new Bundle();
                bundle.putInt(MediaPlayerService.EXTRA_REWIND_TIME, progress);
                intent.putExtras(bundle);
                PendingIntent pendingIntent = PendingIntent.getService(activity,
                        MediaPlayerService.SERVICE_PI_REQ_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                try {
                    pendingIntent.send();
                } catch (CanceledException e) {
                    LOGGER.warning("intent canceled: " + e.getMessage());
                }
            }
        }
    }

    public static String timeToString(int timeMillis) {
        int sec = timeMillis / 1000;
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

}