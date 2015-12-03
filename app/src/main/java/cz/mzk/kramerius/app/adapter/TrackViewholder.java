package cz.mzk.kramerius.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity.PlayerServiceHelper;

public class TrackViewholder {

    private final Context mContext;
    private final PlayerServiceHelper mServiceHelper;
    private Animation mVinylAnimation;
    // views
    private final View mRoot;
    private final TextView mTitle;
    private final TextView mSubtitle;
    private final ImageView mVinyl;
    // binded data
    private SoundRecording mSoundRecording;
    private int mPosition;

    public TrackViewholder(Context context, View itemView, PlayerServiceHelper serviceHelper) {
        this.mContext = context;
        this.mServiceHelper = serviceHelper;
        mRoot = itemView;
        mTitle = (TextView) itemView.findViewById(R.id.track_title);
        mSubtitle = (TextView) itemView.findViewById(R.id.track_subtitle);
        mVinyl = (ImageView) itemView.findViewById(R.id.track_vinyl);
        mVinylAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation);
        mVinylAnimation.setRepeatCount(Animation.INFINITE);
    }

    public void bind(SoundRecording soundRecording, int position) {
        this.mSoundRecording = soundRecording;
        this.mPosition = position;
        inflateViews();
    }

    private void inflateViews() {
        Track track = mSoundRecording.getTrack(mPosition);
        mTitle.setText(track.getTitle());
        // if (track.getSoundUnitTitle() != null && !track.getSoundUnitTitle().equals(track.getTitle())) {
        if (track.getSoundUnitTitle() != null) {
            mSubtitle.setVisibility(View.VISIBLE);
            mSubtitle.setText(track.getSoundUnitTitle());
        } else {
            mSubtitle.setVisibility(View.GONE);
        }
        if (isCurrentTrackActive()) {
            setBackgroundDrawable(R.drawable.track_active);
            mVinyl.setVisibility(View.VISIBLE);
            if (mServiceHelper.isPlayingNow()) {
                Animation animation = mVinyl.getAnimation();
                if (animation == null) {
                    mVinyl.startAnimation(mVinylAnimation);
                }
            } else {
                mVinyl.clearAnimation();
            }
        } else {
            setBackgroundDrawable(R.drawable.track_inactive);
            mVinyl.setVisibility(View.GONE);
            mVinyl.clearAnimation();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundDrawable(int resId) {
        Drawable drawable = ContextCompat.getDrawable(mContext, resId);
        if (Build.VERSION.SDK_INT >= 16) {
            mRoot.setBackground(drawable);
        } else {
            mRoot.setBackgroundDrawable(drawable);
        }
    }

    public boolean isCurrentTrackActive() {
        Track track = mSoundRecording.getTrack(mPosition);
        Track currentTrack = mServiceHelper.getCurrentTrackId();
        return currentTrack != null && currentTrack.equals(track);
    }

}
