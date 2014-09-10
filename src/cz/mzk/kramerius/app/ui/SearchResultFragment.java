package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

public class SearchResultFragment extends BaseFragment implements OnOpenDetailListener {

	private static final String EXTRA_QUERY = "extra_query";

	private static final String TAG = SearchResultFragment.class.getSimpleName();

	private GridView mGridview;
	private String mSearchQuery;
	private GridItemAdapter mAdapter;
	private OnItemSelectedListener mOnItemSelectedListener;

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
		//	ScreenUtil.setInsets(getActivity(), view);
		}
		mGridview = (GridView) view.findViewById(R.id.gridview);

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

	class GetResultTask extends AsyncTask<String, Void, List<Item>> {

		private Context tContext;

		public GetResultTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			startLoaderAnimation();
		}

		@Override
		protected List<Item> doInBackground(String... params) {
			String query = params[0];
			return K5Connector.getInstance().getSearchResult(tContext, query);
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if (tContext == null) {
				return;
			}
			mAdapter = new GridItemAdapter(tContext, result, SearchResultFragment.this);
			stopLoaderAnimation();
			mGridview.setAdapter(mAdapter);
		}

	}

	@Override
	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

}
