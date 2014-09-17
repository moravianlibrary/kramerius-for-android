package cz.mzk.kramerius.app.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;

public class PeriodicalArrayAdapter extends ArrayAdapter<Item> {

	public static final int TYPE_ITEM = 0;
	public static final int TYPE_VOLUME = 1;

	private Context mContext;
	private DisplayImageOptions mOptions;
	private int mType;
	private List<Item> mItems;
	private List<Item> mAllItems;
	private OnOpenDetailListener mOnOpenDetailListener;


	public PeriodicalArrayAdapter(Context context, List<Item> list, int type,OnOpenDetailListener onOpenDetailListener) {		
		super(context, R.layout.item_periodical, R.id.periodical_item_title, list);
		mContext = context;
		mType = type;
		mItems = list;
		mOnOpenDetailListener = onOpenDetailListener;
		mAllItems = new ArrayList<Item>(mItems);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext())
				.build();

		mOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.img_empty).showImageOnLoading(R.drawable.anim_loading).cacheInMemory(true)
				.cacheOnDisk(true).build();

		ImageLoader.getInstance().init(config);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	static class ViewHolder {
		public TextView title;
		public TextView info;
		public ImageView thumb;
		public TextView actionLabel;
		public ImageView actionIcon;
		public ImageView lockIcon;
		public ImageView detailButton;
	}

	public void filter(String prefix) {
		if (prefix == null) {
			mItems.clear();
			mItems.addAll(mAllItems);
		} else {
			mItems.clear();
			for (Item item : mAllItems) {
				if (item.getTitle() == null) {
					continue;
				}
				if (item.getTitle().toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH))) {
					mItems.add(item);
				}
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_periodical, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.periodical_item_title);
			viewHolder.info = (TextView) rowView.findViewById(R.id.periodical_item_info);
			viewHolder.thumb = (ImageView) rowView.findViewById(R.id.periodical_item_thumb);
			viewHolder.actionLabel = (TextView) rowView.findViewById(R.id.periodical_item_actionLabel);
			viewHolder.actionIcon = (ImageView) rowView.findViewById(R.id.periodical_item_actionIcon);
			viewHolder.lockIcon = (ImageView) rowView.findViewById(R.id.periodical_item_lockIcon);
			viewHolder.detailButton = (ImageView) rowView.findViewById(R.id.periodical_item_details);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		final Item item = getItem(position);
		if(item.isPrivate()) {
			holder.lockIcon.setVisibility(View.VISIBLE);
		} else {
			holder.lockIcon.setVisibility(View.GONE);
		}
		if (mType == TYPE_VOLUME) {
			String year = item.getYear() == null ? "" : item.getYear();
			String volumeNumber = item.getVolumeNumber() == null ? "" : item.getVolumeNumber();

			holder.title.setText("Ročník " + volumeNumber);
			holder.info.setText("ROK VYDÁNÍ: " + year);
			holder.actionLabel.setText("Zobrazit čísla");
			holder.actionIcon.setImageResource(R.drawable.ic_attach_green);
		} else if (mType == TYPE_ITEM) {
			String number = item.getIssueNumber();
			if(number == null || number.isEmpty()) {
				number = item.getPartNumber();
			} else {
				number += "/" + item.getPartNumber(); 
			}
			
			String date = item.getPeriodicalItemDate() == null ? "" : item.getPeriodicalItemDate();
			holder.title.setText("Číslo " + number);
			holder.info.setText("DATUM VYDÁNÍ: " + date);
			holder.actionLabel.setText("Otevřít číslo");
			holder.actionIcon.setImageResource(R.drawable.ic_book_green);
		}
		
		holder.detailButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onDetailClick(item.getPid());
			}
		});
		
		String url = K5Api.getThumbnailPath(mContext, item.getPid());
		ImageLoader.getInstance().displayImage(url, holder.thumb, mOptions);

		return rowView;
	}
	
	
	private void onDetailClick(String pid) {
		if (mOnOpenDetailListener != null) {
			mOnOpenDetailListener.onOpenDetail(pid);
		}
	}

}