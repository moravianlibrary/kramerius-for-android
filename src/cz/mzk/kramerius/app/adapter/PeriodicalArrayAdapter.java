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

	public PeriodicalArrayAdapter(Context context, List<Item> list, int type) {
		super(context, R.layout.item_periodical, R.id.periodical_item_title, list);
		mContext = context;
		mType = type;
		mItems = list;
		mAllItems = new ArrayList<Item>(mItems);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext())
				.build();

		mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.anim_loading).cacheInMemory(true)
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
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Item item = getItem(position);
		if (mType == TYPE_VOLUME) {
			holder.title.setText("Ročník " + item.getTitle());
			holder.info.setText("ROK VYDÁNÍ: 1941");
			holder.actionLabel.setText("Zobrazit čísla");
			holder.actionIcon.setImageResource(R.drawable.ic_attach_green);
		} else if (mType == TYPE_ITEM) {
			holder.title.setText("Číslo " + item.getTitle());
			holder.info.setText("DATUM VYDÁNÍ: 3.4.1941");
			holder.actionLabel.setText("Otevřít číslo");
			holder.actionIcon.setImageResource(R.drawable.ic_book_green);
		}
		String url = K5Api.getThumbnailPath(mContext, item.getPid());
		ImageLoader.getInstance().displayImage(url, holder.thumb, mOptions);

		return rowView;
	}

}