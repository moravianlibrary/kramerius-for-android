package cz.mzk.kramerius.app.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ModelUtil;

public class GridItemAdapter extends BaseAdapter {

	private static final String TAG = GridItemAdapter.class.getName();

	private Context mContext;
	private final List<Item> mList;

	private DisplayImageOptions mOptions;
	private OnOpenDetailListener mOnOpenDetailListener;

	public GridItemAdapter(Context context, List<Item> list, OnOpenDetailListener onOpenDetailListener) {
		mContext = context;
		mList = list;
		mOnOpenDetailListener = onOpenDetailListener;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext.getApplicationContext())
				.build();

		mOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.anim_loading).cacheInMemory(true)
				.cacheOnDisk(true).build();

		ImageLoader.getInstance().init(config);
	}

	static class ViewHolder {
		public TextView title;
		public TextView author;
		public TextView type;
		public ImageView thumb;
		public ImageView modelIcon;
		public ImageView lockIcon;
		public View detailButton;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.grid_item, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.grid_item_title);
			viewHolder.author = (TextView) rowView.findViewById(R.id.grid_item_author);
			viewHolder.type = (TextView) rowView.findViewById(R.id.grid_item_type);
			viewHolder.thumb = (ImageView) rowView.findViewById(R.id.grid_item_thumb);
			viewHolder.modelIcon = (ImageView) rowView.findViewById(R.id.grid_item_modeIcon);
			viewHolder.lockIcon = (ImageView) rowView.findViewById(R.id.grid_item_lockIcon);
			viewHolder.detailButton = rowView.findViewById(R.id.grid_item_details_toucharea);
			rowView.setTag(viewHolder);
		}

		final ViewHolder holder = (ViewHolder) rowView.getTag();
		final Item item = mList.get(position);
		if (holder.author != null) {
			holder.author.setText(item.getAuthor());
		}
		holder.detailButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onDetailClick(item.getPid());
			}
		});

		if (item.isPrivate()) {
			holder.lockIcon.setVisibility(View.VISIBLE);
		} else {
			holder.lockIcon.setVisibility(View.GONE);
		}

		holder.title.setText(item.getTitle());
		holder.type.setText(mContext.getString(ModelUtil.getLabel(item.getModel())));
		int modelIconRes = ModelUtil.getIcon(item.getModel());
		holder.modelIcon.setImageResource(modelIconRes);
		String url = K5Api.getThumbnailPath(mContext, item.getPid());
		ImageLoader.getInstance().displayImage(url, holder.thumb, mOptions);
		return rowView;
	}

	private void onDetailClick(String pid) {
		if (mOnOpenDetailListener != null) {
			mOnOpenDetailListener.onOpenDetail(pid);
		}
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
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

	public Item getGridItem(int position) {
		return mList.get(position);
	}

}