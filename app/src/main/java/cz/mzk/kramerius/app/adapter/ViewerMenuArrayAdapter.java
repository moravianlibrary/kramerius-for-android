package cz.mzk.kramerius.app.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.RecentMenuItem;

public class ViewerMenuArrayAdapter extends ArrayAdapter<RecentMenuItem> {

	private Context mContext;

	public ViewerMenuArrayAdapter(Activity context, List<RecentMenuItem> list) {
		super(context, R.layout.recent_menu_row, list);
		mContext = context;
	}

	static class ViewHolder {
		public TextView title;
		public TextView subtitle;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.recent_menu_row, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.item_recent_title);
			viewHolder.subtitle = (TextView) rowView.findViewById(R.id.item_recent_subtitle);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		RecentMenuItem item = getItem(position);
		holder.title.setText(item.getTitle());
		if(item.getSubtitle() == null || item.getSubtitle().isEmpty()) {
			holder.subtitle.setVisibility(View.GONE);
		} else {
			holder.subtitle.setText(item.getSubtitle());
			holder.subtitle.setVisibility(View.VISIBLE);
		}
		return rowView;
	}

}