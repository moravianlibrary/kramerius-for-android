package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Location;
import cz.mzk.kramerius.app.metadata.Part;
import cz.mzk.kramerius.app.metadata.Publisher;
import cz.mzk.kramerius.app.metadata.TitleInfo;
import cz.mzk.kramerius.app.model.Metadata;
import cz.mzk.kramerius.app.util.ModelUtil;

public class MetadataFragment extends BaseFragment {

	private static final String TAG = MetadataFragment.class.getName();

	private ViewGroup mContainer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_metadata, container, false);

		mContainer = (ViewGroup) view.findViewById(R.id.metadata_container);
		mContainer.setVisibility(View.GONE);
		inflateLoader((ViewGroup) view, inflater);
		return view;
	}

	public void assignPid(String pid) {
		new getMetadataTask(getActivity()).execute(pid);
	}

	private void populateTopLevelMetadata(Metadata metadata) {
		if (metadata.getTitleInfo().getTitle() != null) {
			TextView mainTitle = new TextView(getActivity());
			mainTitle.setText(metadata.getTitleInfo().getTitle());
			mainTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_title));
			mainTitle.setTextColor(getResources().getColor(R.color.metadata_title));
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_title_margin_bottom);
			mainTitle.setLayoutParams(llp);
			mContainer.addView(mainTitle);
		}
		addTitleInfo(metadata);
		addAuthors(metadata);
		addPublishers(metadata);
		addLocation(metadata);
		addNotes(metadata);
	}

	private void populatePeriodicalVolume(Metadata metadata) {
		mContainer.addView(createSubtitleView(getString(R.string.metadata_periodical_volume)));
		Part part = metadata.getPart();
		if (part == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_part_volume_number), part.getVolumeNumber());
		addKeyValueView(getString(R.string.metadata_part_volume_date), part.getDate());
	}

	private void populatePeriodicalItem(Metadata metadata) {
		mContainer.addView(createSubtitleView(getString(R.string.metadata_periodical_item)));
		Part part = metadata.getPart();
		if (part == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_part_issue_number), part.getIssueNumber());
		addKeyValueView(getString(R.string.metadata_part_part_number), part.getPartNumber());
		addKeyValueView(getString(R.string.metadata_part_issue_date), part.getDate());
	}

	private void populatePage(Metadata metadata) {
		mContainer.addView(createSubtitleView(getString(R.string.metadata_page)));
		Part part = metadata.getPart();
		if (part == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_page_number), part.getPageNumber());
		addKeyValueView(getString(R.string.metadata_page_index), part.getPageIndex());
	}

	private void populateMetadata(Metadata metadata, String model) {
		if (metadata == null || model == null) {
			return;
		}
		if (model.equals(ModelUtil.PERIODICAL) || model.equals(ModelUtil.MANUSCRIPT)
				|| model.equals(ModelUtil.MONOGRAPH) || model.equals(ModelUtil.SOUND_RECORDING)
				|| model.equals(ModelUtil.MAP) || model.equals(ModelUtil.GRAPHIC)
				|| model.equals(ModelUtil.SHEET_MUSIC) || model.equals(ModelUtil.ARCHIVE)) {
			populateTopLevelMetadata(metadata);
		} else if (model.equals(ModelUtil.PAGE)) {
			populatePage(metadata);
		} else if (model.equals(ModelUtil.PERIODICAL_VOLUME)) {
			populatePeriodicalVolume(metadata);
		} else if (model.equals(ModelUtil.PERIODICAL_ITEM)) {
			populatePeriodicalItem(metadata);
		}

	}

	private void addPublishers(Metadata metadata) {
		if (metadata.getPublishers().isEmpty()) {
			return;
		}
		if (metadata.getPublishers().size() > 1) {
			mContainer.addView(createSubtitleView(getString(R.string.metadata_publishers)));
		} else {
			mContainer.addView(createSubtitleView(getString(R.string.metadata_publisher)));
		}

		for (Publisher publisher : metadata.getPublishers()) {
			addKeyValueView(getString(R.string.metadata_publisher_name), publisher.getName());
			addKeyValueView(getString(R.string.metadata_publisher_date), publisher.getDate());
			addKeyValueView(getString(R.string.metadata_publisher_place), publisher.getPlace());
		}
	}

	private void addNotes(Metadata metadata) {
		if (metadata.getNotes().isEmpty()) {
			return;
		}
		String notes = metadata.writeNotes();
		if (notes.trim().isEmpty()) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_notes)));
		mContainer.addView(createValueView(notes));
	}

	// private void addPart(Metadata metadata) {
	// if (metadata.getPart() == null) {
	// return;
	// }
	// mContainer.addView(createSubtitleView(getString(R.string.metadata_location)));
	// Location location = metadata.getLocation();
	// if (location.getPhysicalLocation() != null) {
	// mContainer.addView(createKeyValueView(getString(R.string.metadata_location_physical),
	// location.getPhysicalLocation()));
	// }
	// for (String shelf : location.getShelfLocatons()) {
	// mContainer.addView(createKeyValueView(getString(R.string.metadata_location_shelf),
	// shelf));
	// }
	// }

	private void addLocation(Metadata metadata) {
		if (metadata.getLocation() == null) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_location)));
		Location location = metadata.getLocation();
		addKeyValueView(getString(R.string.metadata_location_physical), location.getPhysicalLocation());
		for (String shelf : location.getShelfLocatons()) {
			addKeyValueView(getString(R.string.metadata_location_shelf), shelf);
		}
	}

	private void addAuthors(Metadata metadata) {
		if (metadata.getAuthors().isEmpty()) {
			return;
		}
		if (metadata.getAuthors().size() > 1) {
			mContainer.addView(createSubtitleView(getString(R.string.metadata_authors)));
		} else {
			mContainer.addView(createSubtitleView(getString(R.string.metadata_author)));
		}

		for (Author author : metadata.getAuthors()) {
			String s = author.getName() == null ? getString(R.string.metadata_author_unknown) : author.getName();
			if (author.getDate() != null) {
				s += ", " + author.getDate();
			}
			mContainer.addView(createValueView(s));
		}
	}

	private void addTitleInfo(Metadata metadata) {
		TitleInfo info = metadata.getTitleInfo();
		if (info == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_title_info_title), info.getTitle());
		addKeyValueView(getString(R.string.metadata_title_info_subtitle), info.getSubtitle());
		addKeyValueView(getString(R.string.metadata_title_info_part_name), info.getPartName());
		addKeyValueView(getString(R.string.metadata_title_info_part_number), info.getPartNumber());
	}

	private View createSubtitleView(String subtitle) {
		TextView view = new TextView(getActivity());
		view.setText(subtitle);
		view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_subtitle));
		view.setTextColor(getResources().getColor(R.color.metadata_subtitle));
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llp.topMargin = getResources().getDimensionPixelSize(R.dimen.metadata_subtitle_margin_top);
		llp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_subtitle_margin_bottom);
		view.setLayoutParams(llp);
		return view;
	}

	private void addKeyValueView(String key, String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		LinearLayout ll = new LinearLayout(getActivity());
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_value_margin_bottom);
		ll.setLayoutParams(lp);
		TextView keyView = new TextView(getActivity());
		keyView.setText(key + ":");
		keyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_key));
		keyView.setTextColor(getResources().getColor(R.color.metadata_key));
		ll.addView(keyView);
		TextView valueView = new TextView(getActivity());
		valueView.setText(value);
		valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_value));
		valueView.setTextColor(getResources().getColor(R.color.metadata_value));
		LinearLayout.LayoutParams valueViewParames = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		valueViewParames.leftMargin = getResources().getDimensionPixelSize(R.dimen.metadata_value_margin_left);
		valueView.setLayoutParams(valueViewParames);
		ll.addView(valueView);
		mContainer.addView(ll);
	}

	private View createValueView(String value) {
		TextView valueView = new TextView(getActivity());
		valueView.setText(value);
		valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_value));
		valueView.setTextColor(getResources().getColor(R.color.metadata_value));
		LinearLayout.LayoutParams valueViewParames = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		valueViewParames.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_value_margin_bottom);
		valueView.setLayoutParams(valueViewParames);
		return valueView;
	}

	private View createDividerView() {
		View view = new TextView(getActivity());
		view.setBackgroundColor(getResources().getColor(R.color.metadata_divider));

		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
				R.dimen.metadata_divider_width), getResources().getDimensionPixelSize(R.dimen.metadata_divider_heigth));
		llp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_divider_margin_bottom);
		llp.topMargin = getResources().getDimensionPixelSize(R.dimen.metadata_divider_margin_top);
		view.setLayoutParams(llp);
		return view;
	}

	class getMetadataTask extends AsyncTask<String, Void, List<Pair<Metadata, String>>> {

		private Context tContext;

		public getMetadataTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			mContainer.setVisibility(View.GONE);
			startLoaderAnimation();
		}

		@Override
		protected List<Pair<Metadata, String>> doInBackground(String... params) {
			String pid = params[0];
			List<Pair<String, String>> hierarchy = K5Connector.getInstance().getHierarychy(tContext, pid);
			List<Pair<Metadata, String>> hierarchyMetadata = new ArrayList<Pair<Metadata, String>>();
			for (int i = 0; i < hierarchy.size(); i++) {
				Metadata metadata = K5Connector.getInstance().getModsMetadata(tContext, hierarchy.get(i).first);
				hierarchyMetadata.add(new Pair<Metadata, String>(metadata, hierarchy.get(i).second));
			}
			return hierarchyMetadata;
		}

		@Override
		protected void onPostExecute(List<Pair<Metadata, String>> hierachyMetadata) {
			if (hierachyMetadata != null) {
				populateHierarchy(hierachyMetadata);
			}
			stopLoaderAnimation();
			mContainer.setVisibility(View.VISIBLE);
		}
	}

	private void populateHierarchy(List<Pair<Metadata, String>> hierarchy) {
		for (int i = 0; i < hierarchy.size(); i++) {
			populateMetadata(hierarchy.get(i).first, hierarchy.get(i).second);
			if (hierarchy.size() > 0 && i < hierarchy.size() - 1) {
				mContainer.addView(createDividerView());
			}

		}
	}

}