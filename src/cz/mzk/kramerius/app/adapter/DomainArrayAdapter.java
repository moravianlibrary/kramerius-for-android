package cz.mzk.kramerius.app.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Domain;
import cz.mzk.kramerius.app.ui.MainActivity;

public class DomainArrayAdapter extends ArrayAdapter<Domain> {

	private Context mContext;
	private String mCurrentDomain;

	public DomainArrayAdapter(Context context, List<Domain> list, String currentDomain) {
		super(context, R.layout.item_domain, R.id.domain_item_title, list);
		mCurrentDomain = currentDomain;
		mContext = context;
	}

	static class ViewHolder {
		public View clickable;
		public TextView title;
		public TextView subtitle;
		public TextView url;
		public ImageView logo;
		public ImageView selected;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.item_domain, parent, false);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.title = (TextView) rowView.findViewById(R.id.domain_item_title);
			viewHolder.subtitle = (TextView) rowView.findViewById(R.id.domain_item_subtitle);
			viewHolder.url = (TextView) rowView.findViewById(R.id.domain_item_url);
			viewHolder.logo = (ImageView) rowView.findViewById(R.id.domain_item_logo);
			viewHolder.selected = (ImageView) rowView.findViewById(R.id.domain_item_selected);
			viewHolder.clickable = rowView.findViewById(R.id.domain_item_clickable);
			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		final Domain domain = getItem(position);
		holder.title.setText(domain.getTitle());
		holder.subtitle.setText(domain.getSubtitle());
		holder.url.setText(domain.getProtocol() + "://" + domain.getDomain());
		holder.logo.setImageResource(domain.getLogo());
		if (domain.getDomain().equals(mCurrentDomain)) {
			holder.selected.setVisibility(View.VISIBLE);
		} else {
			holder.selected.setVisibility(View.GONE);
		}
		holder.clickable.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
						.putString(getContext().getString(R.string.pref_domain_key), domain.getDomain())
						.putString(getContext().getString(R.string.pref_protocol_key), domain.getProtocol()).commit();
				K5Connector.getInstance().restart();
				Intent intent = new Intent(getContext(), MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				
				getContext().startActivity(intent);
			}
		});

		return rowView;
	}

}