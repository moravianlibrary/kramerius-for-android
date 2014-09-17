package cz.mzk.kramerius.app.adapter;

import java.util.List;

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

public class SoundUnitArrayAdapter extends ArrayAdapter<Item> {

	private Context mContext;
	private DisplayImageOptions mOptions;

	public SoundUnitArrayAdapter(Context context, List<Item> list) {
		super(context, R.layout.item_soundunit, R.id.soundunit_item_title, list);
		mContext = context;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext())
				.build();

		mOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.img_empty).showImageOnLoading(R.drawable.anim_loading).cacheInMemory(true)
				.cacheOnDisk(true).build();

		ImageLoader.getInstance().init(config);
	}

	static class ViewHolder {
		public TextView title;
		public ImageView thumb;
		public ImageView lockIcon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_soundunit, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.soundunit_item_title);
			viewHolder.thumb = (ImageView) rowView.findViewById(R.id.soundunit_item_thumb);
			viewHolder.lockIcon = (ImageView) rowView.findViewById(R.id.soundunit_item_lockIcon);
			rowView.setTag(viewHolder);
		}
		
		

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Item item = getItem(position);
		if(item.isPrivate()) {
			holder.lockIcon.setVisibility(View.VISIBLE);
		} else {
			holder.lockIcon.setVisibility(View.GONE);
		}
		
		holder.title.setText(item.getTitle());
		String url = K5Api.getThumbnailPath(mContext, item.getPid());
		ImageLoader.getInstance().displayImage(url, holder.thumb, mOptions);

		return rowView;
	}

}