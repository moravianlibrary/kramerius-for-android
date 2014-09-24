package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
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
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.OnFilterDeletedListener;
import cz.mzk.kramerius.app.search.SearchDateFilter;
import cz.mzk.kramerius.app.search.SearchDoctypeFilter;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.search.SearchTextFilter;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SearchFragment extends BaseFragment implements OnClickListener, OnFilterDeletedListener {

	private static final int MENU_SEARCH = 101;

	private static final String TAG = SearchFragment.class.getName();

	private Button mAddFilter;
	private CheckBox mCheckPublicOnly;

	private ViewGroup mFilterLayout;

	private OnSearchListener mOnSearchListener;

	private List<SearchFilter> mFilters;

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
			itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
		mFilters = new ArrayList<SearchFilter>();
		mAddFilter = (Button) view.findViewById(R.id.search_addFilter);
		mAddFilter.setOnClickListener(this);
		mCheckPublicOnly = (CheckBox) view.findViewById(R.id.search_check_public);
		mFilterLayout = (ViewGroup) view.findViewById(R.id.search_filters);
		mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.TITLE, getString(R.string.search_filter_name)));
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
		String policy = null;
		if (mCheckPublicOnly.isChecked()) {
			policy = "public";
		}
		SearchQuery query = new SearchQuery().add(SearchQuery.POLICY, policy);

		for (SearchFilter filter : mFilters) {
			filter.addToQuery(query);
		}
		String queryString = query.build();
		Analytics.sendEvent(getActivity(), "search", "query", queryString);
		Log.d(TAG, "query:" + queryString);
		if (mOnSearchListener != null) {
			mOnSearchListener.onSearchQuery(queryString);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mAddFilter) {
			showFilterDialog();
		}
	}

	private void showFilterDialog() {
		String[] allItems = getResources().getStringArray(R.array.search_filter_entries);
		final List<String> itemList = new ArrayList<String>(Arrays.asList(allItems));
		for (SearchFilter filter : mFilters) {
			itemList.remove(filter.getName());
		}
		String[] items = new String[allItems.length - mFilters.size()];

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setItems(itemList.toArray(items), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onAddFilter(itemList.get(which));
				// onAddFilter(getResources().getStringArray(R.array.search_filter_entries)[which]);
			}
		});
		builder.create().show();
	}

	private void onAddFilter(String name) {
		if (name.equals(getResources().getString(R.string.search_filter_name))) {
			mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.TITLE, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_author))) {
			mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.AUTHOR, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_issn))) {
			mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.ISSN, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_ddt))) {
			mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.DDT, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_mdt))) {
			mFilters.add(new SearchTextFilter(getActivity(), mFilterLayout, this, SearchQuery.MDT, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_doctype))) {
			mFilters.add(new SearchDoctypeFilter(getActivity(), mFilterLayout, this, SearchQuery.MODEL, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_year))) {
			mFilters.add(new SearchDateFilter(getActivity(), mFilterLayout, this, SearchQuery.DATE_BEGIN, name));
		}
	}

	@Override
	public void onFilterDeleted(SearchFilter filter) {
		mFilters.remove(filter);
		mFilterLayout.removeView(filter.getView());
	}

}
