package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

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
import cz.mzk.kramerius.app.card.SearchDateCard;
import cz.mzk.kramerius.app.card.SearchDoctypeCard;
import cz.mzk.kramerius.app.card.SearchTextCard;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SearchFragment extends BaseFragment implements OnClickListener {

	private static final int MENU_SEARCH = 101;

	private static final String TAG = SearchFragment.class.getName();

	private Button mAddFilter;
	private CheckBox mCheckPublicOnly;

	private CardListView mSearchListView;
	private CardArrayAdapter mAdapter;

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
		mSearchListView = (CardListView) view.findViewById(R.id.card_list);
		List<Card> list = new ArrayList<Card>();
		mAdapter = new CardArrayAdapter(getActivity(), list);
		mAdapter.setInnerViewTypeCount(15);
		CardUtils.setAnimationAdapter(mAdapter, mSearchListView, CardUtils.ANIM_SWING_RIGHT);
		mAddFilter = (Button) view.findViewById(R.id.search_addFilter);
		mAddFilter.setOnClickListener(this);
		mCheckPublicOnly = (CheckBox) view.findViewById(R.id.search_check_public);
		mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.TITLE, getResources().getString(R.string.search_filter_name), false, 0));
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_search);
	}

	private void search() {
		// validate filters
		for (int i = 0; i < mAdapter.getCount(); i++) {
			SearchFilter filter = (SearchFilter) mAdapter.getItem(i);
			int v = filter.validate();
			if (v != 0) {
				showInvalidFilterDialog(v);
				return;
			}
		}

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

		for (int i = 0; i < mAdapter.getCount(); i++) {
			SearchFilter filter = (SearchFilter) mAdapter.getItem(i);
			filter.addToQuery(query);
		}
		String queryString = query.build();
		Analytics.sendEvent(getActivity(), "search", "query", queryString);
		Log.d(TAG, "query:" + queryString);
		if (mOnSearchListener != null) {
			mOnSearchListener.onSearchQuery(queryString);
		}
	}

	private void showInvalidFilterDialog(int res) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(res).setPositiveButton(R.string.gen_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		builder.create().show();
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
		for (int i = 0; i < mAdapter.getCount(); i++) {
			SearchFilter filter = (SearchFilter) mAdapter.getItem(i);
			itemList.remove(filter.getName());
		}
		String[] items = new String[allItems.length - mAdapter.getCount()];
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
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.TITLE, name, false, 0));
		} else if (name.equals(getResources().getString(R.string.search_filter_author))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.AUTHOR, name, false, 1));
		} else if (name.equals(getResources().getString(R.string.search_filter_issn))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.ISSN, name, false, 2));
		} else if (name.equals(getResources().getString(R.string.search_filter_ddt))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.DDT, name, false, 3));
		} else if (name.equals(getResources().getString(R.string.search_filter_mdt))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.MDT, name, false, 4));
		} else if (name.equals(getResources().getString(R.string.search_filter_doctype))) {
			mAdapter.add(new SearchDoctypeCard(getActivity(), SearchQuery.MODEL, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_year))) {
			mAdapter.add(new SearchDateCard(getActivity(), SearchQuery.DATE_BEGIN, name));
		} else if (name.equals(getResources().getString(R.string.search_filter_isbn))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.ISBN, name, true, 5));
		} else if (name.equals(getResources().getString(R.string.search_filter_signature))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.SIGNATURE, name, true, 6));
		} else if (name.equals(getResources().getString(R.string.search_filter_sysno))) {
			mAdapter.add(new SearchTextCard(getActivity(), SearchQuery.SYSNO, name, true, 7));
		}
	}

}
