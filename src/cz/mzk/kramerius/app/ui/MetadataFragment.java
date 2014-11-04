package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cz.mzk.kramerius.app.BaseActivity;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.data.KrameriusContract;
import cz.mzk.kramerius.app.data.KrameriusContract.InstitutuinEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.LanguageEntry;
import cz.mzk.kramerius.app.data.KrameriusContract.RelatorEntry;
import cz.mzk.kramerius.app.metadata.Author;
import cz.mzk.kramerius.app.metadata.Location;
import cz.mzk.kramerius.app.metadata.Metadata;
import cz.mzk.kramerius.app.metadata.Part;
import cz.mzk.kramerius.app.metadata.Publisher;
import cz.mzk.kramerius.app.metadata.TitleInfo;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.TextUtil;

public class MetadataFragment extends BaseFragment {

	private static final String TAG = MetadataFragment.class.getName();

	private static final int MENU_SHARE = 101;

	private ViewGroup mContainer;

	private String mPid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_metadata, container, false);

		mContainer = (ViewGroup) view.findViewById(R.id.metadata_container);
		mContainer.setVisibility(View.GONE);
		inflateLoader((ViewGroup) view, inflater);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem itemSearch = menu.add(1, MENU_SHARE, 1, "Sdílet");
		itemSearch.setIcon(android.R.drawable.ic_menu_share);
		if (isTablet()) {
			itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} else {
			itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SHARE:
			shareDocument();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void shareDocument() {
		String url = K5Api.getPersistentUrl(getActivity(), mPid);
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, url);
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}

	public void assignPid(String pid) {
		mPid = pid;
		new getMetadataTask(getActivity()).execute(mPid);
	}

	private void populateTopLevelMetadata(Metadata metadata, String model, boolean expandable) {
		if (metadata.getTitleInfo().getTitle() != null) {
			TextView mainTitle = new TextView(getActivity());
			mainTitle.setTextIsSelectable(true);
			mainTitle.setText(metadata.getTitleInfo().getTitle());
			mainTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_title));
			mainTitle.setTextColor(getResources().getColor(R.color.metadata_title));
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.metadata_title_margin_bottom);
			mainTitle.setLayoutParams(llp);
			mContainer.addView(mainTitle);
		}

		View.OnClickListener onClick = null;
		final String pid = metadata.getPid();
		if (expandable && ModelUtil.PERIODICAL.equals(model)) {
			onClick = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), PeriodicalActivity.class);
					intent.putExtra(BaseActivity.EXTRA_PID, pid);
					startActivity(intent);
				}
			};
		}
		mContainer.addView(createModelHeaderView(getString(ModelUtil.getLabel(model)), expandable, onClick));

		addTitleInfo(metadata);
		addIdentifiers(metadata);
		addLanguages(metadata);
		addAuthors(metadata);
		addPublishers(metadata);
		addAbstract(metadata);
		addKeywords(metadata);
		addLocation(metadata);
		addPhysicalDescription(metadata);
		addCartographics(metadata);
		addNotes(metadata);
	}

	private void populatePeriodicalVolume(Metadata metadata, boolean expandable) {
		View.OnClickListener onClick = null;
		final String pid = metadata.getPid();
		if (expandable) {
			onClick = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), PeriodicalActivity.class);
					intent.putExtra(BaseActivity.EXTRA_PID, pid);
					startActivity(intent);
				}
			};
		}
		mContainer.addView(createModelHeaderView(getString(ModelUtil.getLabel(ModelUtil.PERIODICAL_VOLUME)),
				expandable, onClick));

		Part part = metadata.getPart();
		if (part == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_part_volume_number), part.getVolumeNumber());
		addKeyValueView(getString(R.string.metadata_part_volume_date), part.getDate());
		addNotes(metadata);
	}

	private void populatePeriodicalItem(Metadata metadata) {
		mContainer
				.addView(createModelHeaderView(getString(ModelUtil.getLabel(ModelUtil.PERIODICAL_ITEM)), false, null));
		Part part = metadata.getPart();
		String date = null;
		String issue = null;
		if (part != null) {
			date = part.getDate();
			issue = part.getIssueTitle();
		}
		if (issue == null || issue.isEmpty()) {
			issue = metadata.getTitleInfo().getPartName();
			;
		}

		if (date == null || date.isEmpty()) {
			if (!metadata.getPublishers().isEmpty()) {
				date = metadata.getPublishers().get(0).getDate();
			}
		}

		addKeyValueView(getString(R.string.metadata_part_issue_number), issue);
		addKeyValueView(getString(R.string.metadata_part_issue_date), date);
		addNotes(metadata);
	}

	private void populatePage(Metadata metadata) {
		// mContainer.addView(createSubtitleView(getString(R.string.metadata_page)));
		mContainer.addView(createModelHeaderView(getString(ModelUtil.getLabel(ModelUtil.PAGE)), false, null));

		Part part = metadata.getPart();
		if (part == null) {
			return;
		}
		addKeyValueView(getString(R.string.metadata_page_number), part.getPageNumber());
		addKeyValueView(getString(R.string.metadata_page_index), part.getPageIndex());
		addNotes(metadata);
	}

	private void populateMetadata(Metadata metadata, String model, boolean expandable) {
		if (metadata == null || model == null) {
			return;
		}
		if (model.equals(ModelUtil.PERIODICAL) || model.equals(ModelUtil.MANUSCRIPT)
				|| model.equals(ModelUtil.MONOGRAPH) || model.equals(ModelUtil.SOUND_RECORDING)
				|| model.equals(ModelUtil.MAP) || model.equals(ModelUtil.GRAPHIC)
				|| model.equals(ModelUtil.SHEET_MUSIC) || model.equals(ModelUtil.ARCHIVE)) {
			populateTopLevelMetadata(metadata, model, expandable);

		} else if (model.equals(ModelUtil.PAGE)) {
			populatePage(metadata);
		} else if (model.equals(ModelUtil.PERIODICAL_VOLUME)) {
			populatePeriodicalVolume(metadata, expandable);
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

	private void addPhysicalDescription(Metadata metadata) {
		if (metadata.getPhysicalDescription() == null) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_physical_description)));
		addKeyValueView(getString(R.string.metadata_physical_description_scale), metadata.getPhysicalDescription()
				.getScale());
		addKeyValueView(getString(R.string.metadata_physical_description_extent), metadata.getPhysicalDescription()
				.getExtent());

		if (!metadata.getPhysicalDescription().getNotes().isEmpty()) {
			String notes = TextUtil.writeNotes(metadata.getPhysicalDescription().getNotes());
			if (!notes.isEmpty()) {
				mContainer.addView(createValueView(notes));
			}

		}
	}

	private void addCartographics(Metadata metadata) {
		if (metadata.getCartographics() == null) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_cartographics)));
		addKeyValueView(getString(R.string.metadata_cartographics_scale), metadata.getCartographics().getScale());
		addKeyValueView(getString(R.string.metadata_cartographics_coordinates), metadata.getCartographics()
				.getCoordinates());
	}

	private void addAbstract(Metadata metadata) {
		if (metadata.getAbstract() == null) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_abstract)));
		mContainer.addView(createValueView(metadata.getAbstract()));
	}

	private void addNotes(Metadata metadata) {
		if (metadata.getNotes().isEmpty()) {
			return;
		}
		String notes = TextUtil.writeNotes(metadata.getNotes());
		if (notes.trim().isEmpty()) {
			return;
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_notes)));
		mContainer.addView(createValueView(notes));
	}

	private void addKeywords(Metadata metadata) {
		if (metadata.getKeywords().isEmpty()) {
			return;
		}
		String keywords = "";
		for (int i = 0; i < metadata.getKeywords().size(); i++) {
			keywords += metadata.getKeywords().get(i);
			if (i < metadata.getKeywords().size() - 1) {
				keywords += ", ";
			}
		}
		mContainer.addView(createSubtitleView(getString(R.string.metadata_keywords)));
		mContainer.addView(createValueView(keywords));
	}

	private void addLanguages(Metadata metadata) {
		if (metadata.getLanguages().isEmpty()) {
			return;
		}
		String title = getString(R.string.metadata_language);
		if (metadata.getLanguages().size() > 1) {
			title = getString(R.string.metadata_languages);
		}
		String languages = "";
		for (int i = 0; i < metadata.getLanguages().size(); i++) {

			String language = metadata.getLanguages().get(i);
			Cursor c = getActivity().getContentResolver().query(KrameriusContract.LanguageEntry.CONTENT_URI,
					new String[] { LanguageEntry.COLUMN_NAME }, LanguageEntry.COLUMN_CODE + "=?",
					new String[] { language }, null);
			if (c.moveToFirst()) {
				String l = c.getString(0);
				if (l != null && !l.isEmpty()) {
					language = l;
				}
			}
			c.close();

			languages += language;

			if (i < metadata.getLanguages().size() - 1) {
				languages += ", ";
			}
		}
		addKeyValueView(title, languages);
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

		if (location.getPhysicalLocation() != null && !location.getPhysicalLocation().isEmpty()
				&& getActivity() != null) {
			Cursor c = getActivity().getContentResolver().query(KrameriusContract.InstitutuinEntry.CONTENT_URI,
					new String[] { InstitutuinEntry.COLUMN_NAME }, InstitutuinEntry.COLUMN_SIGLA + "=?",
					new String[] { location.getPhysicalLocation() }, null);
			if (c.moveToFirst()) {
				String phyciscalLocation = c.getString(c.getColumnIndex(InstitutuinEntry.COLUMN_NAME));
				addKeyValueView(getString(R.string.metadata_location_physical), phyciscalLocation);
			}
			c.close();
		}
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
			if (!author.getRoleCodes().isEmpty()) {
				s += " (";
				for (int i = 0; i < author.getRoleCodes().size(); i++) {
					String role = author.getRoleCodes().get(i);
					// int resId = getResources()
					// .getIdentifier("author_" + role, "string",
					// getActivity().getPackageName());
					// if (resId != 0) {
					// role = getString(resId);
					// }
					Cursor c = getActivity().getContentResolver().query(KrameriusContract.RelatorEntry.CONTENT_URI,
							new String[] { RelatorEntry.COLUMN_NAME }, RelatorEntry.COLUMN_CODE + "=?",
							new String[] { role }, null);
					if (c.moveToFirst()) {
						String r = c.getString(0);
						if (r != null && !r.isEmpty()) {
							role = r;
						}
					}
					c.close();
					s += role;
					if (i < author.getRoleCodes().size() - 1) {
						s += ", ";
					}
				}
				s += ")";
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

	private void addIdentifiers(Metadata metadata) {
		addKeyValueView(getString(R.string.metadata_identifier_issn), metadata.getIssn());
		addKeyValueView(getString(R.string.metadata_identifier_isbn), metadata.getIsbn());
		addKeyValueView(getString(R.string.metadata_identifier_ccnb), metadata.getCcnb());
		addKeyValueView(getString(R.string.metadata_identifier_oclc), metadata.getOclc());
	}

	private View createSubtitleView(String subtitle) {
		TextView view = new TextView(getActivity());
		view.setTextIsSelectable(true);
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

	private View createModelHeaderView(String title, boolean expandable, View.OnClickListener onClick) {
		RelativeLayout rl = new RelativeLayout(getActivity());
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		// rlp.topMargin =
		// getResources().getDimensionPixelSize(R.dimen.metadata_model_header_margin_top);
		// rlp.bottomMargin =
		// getResources().getDimensionPixelSize(R.dimen.metadata_model_header_margin_bottom);
		rl.setLayoutParams(rlp);

		rl.setPadding(0, getResources().getDimensionPixelSize(R.dimen.metadata_model_header_margin_top), 0,
				getResources().getDimensionPixelSize(R.dimen.metadata_model_header_margin_bottom));

		TextView textView = new TextView(getActivity());
		textView.setTextIsSelectable(true);
		textView.setText(title);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_subtitle));
		textView.setTextColor(getResources().getColor(R.color.metadata_subtitle));

		rl.addView(textView);

		if (expandable) {
			Button button = new Button(getActivity());
			button.setText(getString(R.string.metadata_open_parent));
			button.setBackgroundColor(getResources().getColor(R.color.green));
			button.setTextColor(getResources().getColor(R.color.white));
			button.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					getResources().getDimensionPixelSize(R.dimen.metadata_button_text_size));

			button.setPadding(0, 0, 0, 0);

			button.setGravity(Gravity.CENTER);

			if (onClick != null) {
				button.setOnClickListener(onClick);
			}

			RelativeLayout.LayoutParams buttonRlp = new RelativeLayout.LayoutParams(getResources()
					.getDimensionPixelSize(R.dimen.metadata_button_width), getResources().getDimensionPixelSize(
					R.dimen.metadata_button_height));
			buttonRlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			rl.addView(button, buttonRlp);

		}

		return rl;
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
		keyView.setTextIsSelectable(true);
		keyView.setText(key + ":");
		keyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.metadata_key));
		keyView.setTextColor(getResources().getColor(R.color.metadata_key));
		ll.addView(keyView);
		TextView valueView = new TextView(getActivity());
		valueView.setTextIsSelectable(true);
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
		valueView.setTextIsSelectable(true);
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
			if (hierarchy == null) {
				return null;
			}
			List<Pair<Metadata, String>> hierarchyMetadata = new ArrayList<Pair<Metadata, String>>();
			for (int i = 0; i < hierarchy.size(); i++) {
				Metadata metadata = K5Connector.getInstance().getModsMetadata(tContext, hierarchy.get(i).first);
				hierarchyMetadata.add(new Pair<Metadata, String>(metadata, hierarchy.get(i).second));
			}
			return hierarchyMetadata;
		}

		@Override
		protected void onPostExecute(List<Pair<Metadata, String>> hierachyMetadata) {
			stopLoaderAnimation();
			if (hierachyMetadata == null || getActivity() == null) {
				showWarningMessage("Nepodařilo se načíst data.", "Opakovat", new onWarningButtonClickedListener() {
					@Override
					public void onWarningButtonClicked() {
						new getMetadataTask(getActivity()).execute(mPid);
					}
				});
				return;
			}
			mContainer.setVisibility(View.VISIBLE);
			populateHierarchy(hierachyMetadata);
		}
	}

	private void populateHierarchy(List<Pair<Metadata, String>> hierarchy) {
		for (int i = 0; i < hierarchy.size(); i++) {
			boolean expandable = i < hierarchy.size() - 2;
			populateMetadata(hierarchy.get(i).first, hierarchy.get(i).second, expandable);
			if (hierarchy.size() > 0 && i < hierarchy.size() - 1) {
				mContainer.addView(createDividerView());
			}

		}
	}

}
