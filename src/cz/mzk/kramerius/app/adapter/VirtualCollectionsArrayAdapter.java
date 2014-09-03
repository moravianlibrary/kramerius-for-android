package cz.mzk.kramerius.app.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.model.Item;

public class VirtualCollectionsArrayAdapter extends ArrayAdapter<Item> {

	private Context mContext;

	public VirtualCollectionsArrayAdapter(Context context, List<Item> list) {
		super(context, R.layout.item_virtual_collection, R.id.vc_item_title, list);
		mContext = context;
	}

	static class ViewHolder {
		public TextView title;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_virtual_collection, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.vc_item_title);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Item so = getItem(position);
		holder.title.setText(so.getTitle());

		return rowView;
	}

}