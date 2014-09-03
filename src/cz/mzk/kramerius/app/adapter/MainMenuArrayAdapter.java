package cz.mzk.kramerius.app.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.mzk.kramerius.app.MenuListItem;
import cz.mzk.kramerius.app.R;

public class MainMenuArrayAdapter extends ArrayAdapter<MenuListItem> {

	private Context mContext;
	private int mSelection;
	private List<MenuListItem> mList;

	public MainMenuArrayAdapter(Activity context, List<MenuListItem> list) {
		super(context, R.layout.main_menu_row, R.id.menu_row_title, list);
		mContext = context;
		mList = list;
		mSelection = 0;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}
	
	public void setSelection(int position) {
		mSelection = position;
		notifyDataSetChanged();
	}

	static class ViewHolder {
		public TextView title;
		public ImageView icon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.main_menu_row, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.menu_row_title);
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_row_icon);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		MenuListItem item = getItem(position);
		holder.title.setText(item.getTitle());

		if (position == mSelection) {
			holder.title.setTextColor(mContext.getResources().getColor(R.color.green));
			holder.icon.setImageResource(item.getIconResourceSelected());
		} else {
			holder.title.setTextColor(mContext.getResources().getColor(R.color.grey));
			holder.icon.setImageResource(item.getIconResource());
		}
		return rowView;
	}

}