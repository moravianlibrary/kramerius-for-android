package cz.mzk.kramerius.app.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;

public class PageSelectionAdapter extends BaseAdapter {

	private static final String TAG = PageSelectionAdapter.class.getName();

	private Context mContext;
	private final List<Item> mList;

	private DisplayImageOptions mOptions;

	public PageSelectionAdapter(Context context, List<Item> list) {
		mContext = context;
		mList = list;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext())
				.build();

		mOptions = new DisplayImageOptions.Builder().showImageOnFail(R.drawable.img_empty).showImageOnLoading(R.drawable.anim_loading).cacheInMemory(true)
				.cacheOnDisk(true).build();

		ImageLoader.getInstance().init(config);
	}

	static class ViewHolder {
		public TextView title;
		public ImageView thumb;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_page_selection, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.page_selection_item_title);
			viewHolder.thumb = (ImageView) rowView.findViewById(R.id.page_selection_item_thumb);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Item item = mList.get(position);
		holder.title.setText(item.getTitle());
		String url = K5Api.getThumbnailPath(mContext, item.getPid());
		ImageLoader.getInstance().displayImage(url, holder.thumb, mOptions);
		return rowView;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}