package cz.mzk.kramerius.app.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ModelUtil;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SearchFragment extends BaseFragment implements OnClickListener {

	private static final int MENU_SEARCH = 101;

	private static final String TAG = SearchFragment.class.getName();

	private Button mAdvanced;
	private EditText mInputTitle;
	private EditText mInputAuthor;
	private CheckBox mCheckPublicOnly;
	private Spinner mSpinnerType;
	private EditText mInputDateFrom;
	private EditText mInputDateTo;
	
	private OnSearchListener mOnSearchListener;

	
	public interface OnSearchListener {
		public void onSearchQuery(String query);
	}

	
	public void setOnSearchListener(OnSearchListener onSearchListener) {
		mOnSearchListener = onSearchListener;
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		MenuItem itemSearch = menu.add(1, MENU_SEARCH, 1, "Vyhledat");
		itemSearch.setIcon(android.R.drawable.ic_menu_send);
		if (isTablet()) {
			itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		} else {
			itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SEARCH:
			search();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);
		Configuration config = getResources().getConfiguration();
		if (config.smallestScreenWidthDp < 720) {
			ScreenUtil.setInsets(getActivity(), view);
		}
		mSpinnerType = (Spinner) view.findViewById(R.id.search_spinner_type);
		mSpinnerType.setSelection(0);
		mAdvanced = (Button) view.findViewById(R.id.search_advanced);
		mAdvanced.setOnClickListener(this);
		mInputTitle = (EditText) view.findViewById(R.id.search_input_title);
		mInputAuthor = (EditText) view.findViewById(R.id.search_input_author);
		mInputDateFrom = (EditText) view.findViewById(R.id.search_date_from);
		mInputDateTo = (EditText) view.findViewById(R.id.search_date_to);
		mCheckPublicOnly = (CheckBox) view.findViewById(R.id.search_check_public);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_search);
	}

	private void search() {
		InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		View view = getActivity().getCurrentFocus();
		if (view != null) {
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		String title = mInputTitle.getText().toString().toLowerCase();
		String author = mInputAuthor.getText().toString().toLowerCase();
		
		
		
		
		
		String policy = null;
		if (mCheckPublicOnly.isChecked()) {
			policy = "public";
		}

		int typePosition = mSpinnerType.getSelectedItemPosition();
		String type = null;
		switch (typePosition) {
		case 0:
			type = ModelUtil.MONOGRAPH;
			break;
		case 1:
			type = ModelUtil.PERIODICAL;
			break;
		case 2:
			type = ModelUtil.MANUSCRIPT;
			break;
		case 3:
			type = ModelUtil.MAP;
			break;
		case 4:
			type = ModelUtil.GRAPHIC;
			break;
		case 5:
			type = ModelUtil.SHEET_MUSIC;
			break;
		case 6:
			type = ModelUtil.ARCHIVE;
			break;
		case 7:
			type = ModelUtil.SOUND_RECORDING;
			break;
		}

		SearchQuery query = new SearchQuery().add(SearchQuery.TITLE, title).add(SearchQuery.AUTHOR, author)
				.add(SearchQuery.POLICY, policy);
		
		if (type != null) {
			query.add(SearchQuery.MODEL, type, false);			
		}
		String dateFrom = mInputDateFrom.getText().toString();
		String dateTo = mInputDateTo.getText().toString();
		try {
			int begin = Integer.valueOf(dateFrom);
			int end = Integer.valueOf(dateTo);
			query.date(begin, end);			
		} catch (NumberFormatException ex) {
			
		}
		
		
		Analytics.sendEvent(getActivity(), "search", "query", query.build());
		
		if (type == null) {
			query.allModels();
		} 
		if (mOnSearchListener != null) {
			mOnSearchListener.onSearchQuery(query.build());
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mAdvanced) {
		}

	}

}
