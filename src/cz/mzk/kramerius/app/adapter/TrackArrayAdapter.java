package cz.mzk.kramerius.app.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;

public class TrackArrayAdapter extends ArrayAdapter<Item> {

	private Context mContext;
	private int mSelection;
	private Animation mVinylAnimation;
	private boolean mPlaying;

	public TrackArrayAdapter(Context context, List<Item> list) {
		super(context, R.layout.item_track, R.id.track_title, list);
		mContext = context;
		mSelection = -1;
		mPlaying = false;
		mVinylAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotation);
		mVinylAnimation.setRepeatCount(Animation.INFINITE);
	}

	public void setSelection(int position) {
		mSelection = position;
		notifyDataSetChanged();
	}
	
	public int getSelection() {
		return mSelection;
	}
	
	public void setPlaying(boolean playing) {
		mPlaying = playing;
		notifyDataSetChanged();
	}

	static class ViewHolder {
		public TextView title;
		public ImageView image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_track, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.track_title);
			viewHolder.image = (ImageView) rowView.findViewById(R.id.track_vinyl);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Item item = getItem(position);
		holder.title.setText(item.getTitle());
		holder.image.clearAnimation();
		if (position == mSelection) {
			holder.title.setTextColor(mContext.getResources().getColor(R.color.dark_dark_grey));
			if(mPlaying) {
				holder.image.startAnimation(mVinylAnimation);
			}
			holder.image.setVisibility(View.VISIBLE);
		} else {
			holder.title.setTextColor(mContext.getResources().getColor(R.color.grey));
			holder.image.setVisibility(View.GONE);
		}
		return rowView;
	}

}