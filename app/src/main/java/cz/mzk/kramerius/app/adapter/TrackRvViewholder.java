package cz.mzk.kramerius.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.SoundRecording;
import cz.mzk.kramerius.app.model.Track;
import cz.mzk.kramerius.app.service.MediaPlayerService;
import cz.mzk.kramerius.app.ui.SoundRecordingActivity.PlayerServiceHelper;

public class TrackRvViewholder extends RecyclerView.ViewHolder implements OnClickListener {

    // permanent data
    private final Context mContext;
    private final PlayerServiceHelper mServiceHelper;
    private final Animation mVinylAnimation;
    private final float mElevationActive;
    private final float mElevationInactive;
    // views
    private final CardView mCard;
    private final View mContainer;
    private final TextView mTitle;
    private final TextView mSubtitle;
    private final ImageView mVinyl;
    // binded data
    private SoundRecording mSoundRecording;
    private int mPosition;

    public TrackRvViewholder(Context context, View itemView, PlayerServiceHelper serviceHelper) {
        super(itemView);
        this.mContext = context;
        this.mServiceHelper = serviceHelper;
        mCard = (CardView) itemView;
        mCard.setOnClickListener(this);
        mContainer = itemView.findViewById(R.id.container);
        mTitle = (TextView) itemView.findViewById(R.id.track_title);
        mSubtitle = (TextView) itemView.findViewById(R.id.track_subtitle);
        mVinyl = (ImageView) itemView.findViewById(R.id.track_vinyl);
        mVinylAnimation = AnimationUtils.loadAnimation(context, R.anim.rotation);
        mVinylAnimation.setRepeatCount(Animation.INFINITE);
        Resources res = context.getResources();
        mElevationActive = res.getDimension(R.dimen.track_rv_card_elevation_active);
        mElevationInactive = res.getDimension(R.dimen.track_rv_card_elevation_inactive);
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
            setBackgroundDrawable(R.drawable.track_rv_active);
            mCard.setCardElevation(mElevationActive);
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
            setBackgroundDrawable(R.drawable.track_rv_inactive);
            mCard.setCardElevation(mElevationInactive);
            mVinyl.setVisibility(View.GONE);
            mVinyl.clearAnimation();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundDrawable(int resId) {
        Drawable drawable = ContextCompat.getDrawable(mContext, resId);
        if (Build.VERSION.SDK_INT >= 16) {
            mContainer.setBackground(drawable);
        } else {
            mContainer.setBackgroundDrawable(drawable);
        }
    }

    public boolean isCurrentTrackActive() {
        Track track = mSoundRecording.getTrack(mPosition);
        Track currentTrack = mServiceHelper.getCurrentTrackId();
        return currentTrack != null && currentTrack.equals(track);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(mContext, MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_PLAY);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MediaPlayerService.EXTRA_PLAY_SOUND_RECORDING, mSoundRecording);
        bundle.putInt(MediaPlayerService.EXTRA_PLAY_POSITION, mPosition);
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

}
