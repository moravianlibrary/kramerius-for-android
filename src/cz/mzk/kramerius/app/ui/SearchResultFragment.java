package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.GridItemAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class SearchResultFragment extends BaseFragment implements OnOpenDetailListener, OnScrollListener {

	private static final String EXTRA_QUERY = "extra_query";

	private static final String TAG = SearchResultFragment.class.getSimpleName();

	private GridView mGridview;
	private String mSearchQuery;
	private GridItemAdapter mAdapter;
	private OnItemSelectedListener mOnItemSelectedListener;

	private boolean mLoading;
	private int mRows = 30;
	private int mStart = 0;
	private int mNumFound = -1;
	private boolean mFirst = true;

	public static SearchResultFragment newInstance(String query) {
		SearchResultFragment f = new SearchResultFragment();
		Bundle args = new Bundle();
		args.putString(EXTRA_QUERY, query);
		f.setArguments(args);
		return f;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSearchQuery = getArguments().getString(EXTRA_QUERY, "*:*");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_featured, container, false);
		Configuration config = getResources().getConfiguration();
		if (isPhone()) {
			// ScreenUtil.setInsets(getActivity(), view);
		}
		mGridview = (GridView) view.findViewById(R.id.gridview);
		mGridview.setOnScrollListener(this);
		mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (mAdapter == null) {
					return;
				}
				final Item item = mAdapter.getGridItem(position);
				onItemSelected(item);
			}
		});
		inflateLoader(container, inflater);
		mLoading = true;
		new GetResultTask(getActivity()).execute(mSearchQuery);
		return view;
	}

	private void onItemSelected(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_search_result);
	}

	class GetResultTask extends AsyncTask<String, Void, Pair<List<Item>, Integer>> {

		private Context tContext;

		public GetResultTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			if(mFirst) {
				startLoaderAnimation();
			}
		}

		@Override
		protected Pair<List<Item>, Integer> doInBackground(String... params) {
			String query = params[0];
			return K5Connector.getInstance().getSearchResult(tContext, query, mStart, mRows);
		}

		@Override
		protected void onPostExecute(Pair<List<Item>, Integer> result) {
			if (tContext == null || result == null || result.first == null) {
				return;
			}
			if (result.second == 0) {
				// TODO empty result set
			}
			mNumFound = result.second;
			if (mAdapter == null) {
				mAdapter = new GridItemAdapter(tContext, result.first, SearchResultFragment.this);
			} else {

				int index = mGridview.getFirstVisiblePosition();
				mAdapter.addAll(result.first);
				mGridview.setSelection(index);
			}
			if(mFirst) {
				stopLoaderAnimation();
			}
			mGridview.setAdapter(mAdapter);
			mLoading = false;
			mFirst = false;
			int shownNum = mStart + mRows;
			if(shownNum > mNumFound) {
				shownNum = mNumFound;
			}
			getActivity().getActionBar().setSubtitle(shownNum + " z " + mNumFound);
		}

	}

	@Override
	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (mFirst || mLoading) {
			return;
		}
		boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
		boolean hasMore = mStart + mRows < mNumFound;
		if (loadMore && hasMore) {
			mStart += mRows;
			mLoading = true;
			new GetResultTask(getActivity()).execute(mSearchQuery);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

}
