package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.search.DateSearchFilter;
import cz.mzk.kramerius.app.search.DoctypeSearchFilter;
import cz.mzk.kramerius.app.search.InputSearchFilter;
import cz.mzk.kramerius.app.search.LanguageSearchFilter;
import cz.mzk.kramerius.app.search.SearchFilter;
import cz.mzk.kramerius.app.search.SearchQuery;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.PrefUtils;
import cz.mzk.kramerius.app.util.VersionUtils;

public class SearchFragment extends BaseFragment implements OnClickListener {

	private static final String TAG = SearchFragment.class.getName();

	public static final String EXTRA_TYPE = "extra_type";
	public static final int TYPE_BASIC = 0;
	public static final int TYPE_FULLTEXT = 1;

	private CheckBox mCheckPublicOnly;

	private OnSearchListener mOnSearchListener;

	private View mGoButton;
	private View mAddFilterButton;

	private ViewGroup mFilterContainer;

	private List<SearchFilter> mFilters;

	private int mType;

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
		mType = getArguments().getInt(EXTRA_TYPE, TYPE_BASIC);
	}

	public static SearchFragment getInstance(int type) {
		SearchFragment f = new SearchFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRA_TYPE, type);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search, container, false);

		mFilters = new ArrayList<SearchFilter>();
		mFilterContainer = (ViewGroup) view.findViewById(R.id.search_filter_container);

		mGoButton = view.findViewById(R.id.search_go);
		mGoButton.setOnClickListener(this);
		mAddFilterButton = view.findViewById(R.id.search_add_filter);
		mAddFilterButton.setOnClickListener(this);
		mCheckPublicOnly = (CheckBox) view.findViewById(R.id.search_check_public);

		mCheckPublicOnly.setChecked(PrefUtils.isPublicOnly(getActivity()));

		if (mType == TYPE_FULLTEXT) {
			// mAddFilterButton.setVisibility(View.GONE);
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, false, SearchQuery.OCR, getResources()
					.getString(R.string.search_filter_fulltext), false);
		} else {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.TITLE, getResources()
					.getString(R.string.search_filter_name), false);
			new DoctypeSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.MODEL, getResources()
					.getString(R.string.search_filter_doctype), false);

			// mAddFilterButton.setVisibility(View.VISIBLE);
		}

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_search);
	}

	private void search() {
		// validate filters
		for (SearchFilter filter : mFilters) {
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

		for (SearchFilter filter : mFilters) {
			filter.addToQuery(query);
		}
		query.fulltext(mType == TYPE_FULLTEXT);
		String queryString = query.build();
		Analytics.sendEvent(getActivity(), "search", "query", queryString);
		if(VersionUtils.Debuggable()) {
			Log.d(TAG, "query:" + queryString);
		}
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
		if (v == mGoButton) {
			search();
		} else if (v == mAddFilterButton) {
			showFilterDialog();
		}
	}

	private void showFilterDialog() {
		String[] allItems = null;
		if (mType == TYPE_BASIC) {
			allItems = getResources().getStringArray(R.array.search_filter_entries);
		} else {
			allItems = getResources().getStringArray(R.array.search_fulltext_filter_entries);
		}
		final List<String> itemList = new ArrayList<String>(Arrays.asList(allItems));
		for (SearchFilter filter : mFilters) {
			itemList.remove(filter.getName());
		}
		String[] items = new String[allItems.length - mFilters.size()];
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setItems(itemList.toArray(items), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onAddFilter(itemList.get(which));
			}
		});
		builder.create().show();
	}

	private void onAddFilter(String name) {
		if (name.equals(getResources().getString(R.string.search_filter_name))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.TITLE, name, false);
		} else if (name.equals(getResources().getString(R.string.search_filter_author))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.AUTHOR, name, false);
		} else if (name.equals(getResources().getString(R.string.search_filter_issn))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.ISSN, name, false);
		} else if (name.equals(getResources().getString(R.string.search_filter_doctype))) {
			if (mType == TYPE_FULLTEXT) {
				new DoctypeSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.MODEL_PATH, name,
						true);
			} else {
				new DoctypeSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.MODEL, name, false);
			}
		} else if (name.equals(getResources().getString(R.string.search_filter_language))) {
			new LanguageSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.LANGUAGE, name);
		} else if (name.equals(getResources().getString(R.string.search_filter_year))) {
			new DateSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.YEAR, name);
		} else if (name.equals(getResources().getString(R.string.search_filter_isbn))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.ISBN, name, true);
		} else if (name.equals(getResources().getString(R.string.search_filter_keyword))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.KEYWORDS, name, false);
		} else if (name.equals(getResources().getString(R.string.search_filter_ccnb))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.CCNB, name, true);
		} else if (name.equals(getResources().getString(R.string.search_filter_signature))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.SIGNATURE, name, true);
		} else if (name.equals(getResources().getString(R.string.search_filter_sysno))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.SYSNO, name, true);
		} else if (name.equals(getResources().getString(R.string.search_filter_fulltext))) {
			new InputSearchFilter(getActivity(), mFilterContainer, mFilters, true, SearchQuery.OCR, name, false);
		}
	}

}
