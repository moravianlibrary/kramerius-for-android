package cz.mzk.kramerius.app.ui;

import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.adapter.GridItemAdapter;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class FeaturedFragment extends BaseFragment {

	private static final String EXTRA_TYPE = "extra_type";

	private static final String TAG = FeaturedFragment.class.getSimpleName();

	private GridView mGridview;
	private int mType;
	private GridItemAdapter mAdapter;
	private OnItemSelectedListener mOnItemSelectedListener;

	public static FeaturedFragment newInstance(int type) {
		FeaturedFragment f = new FeaturedFragment();
		Bundle args = new Bundle();
		args.putInt(EXTRA_TYPE, type);
		f.setArguments(args);
		return f;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getArguments().getInt(EXTRA_TYPE, K5Api.FEED_NEWEST);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_featured, container, false);
		Configuration config = getResources().getConfiguration();
		if (isPhone()) {
			ScreenUtil.setInsets(getActivity(), view);
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
		new GetFeaturedTask(getActivity()).execute(mType);
		return view;
	}

	private void onItemSelected(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	class GetFeaturedTask extends AsyncTask<Integer, Void, List<Item>> {

		private Context tContext;

		public GetFeaturedTask(Context context) {
			tContext = context;
		}

		@Override
		protected void onPreExecute() {
			startLoaderAnimation();
		}

		@Override
		protected List<Item> doInBackground(Integer... params) {
			if (params[0] == K5Api.FEED_NEWEST) {
				return K5Connector.getInstance().getNewest(tContext, true, -1);
			} else if (params[0] ==  K5Api.FEED_MOST_DESIRABLE) {
				return K5Connector.getInstance().getMostDesirable(tContext, true, -1);
			} else {
				return K5Connector.getInstance().getSelected(tContext, true, -1);
			}
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if (tContext == null) {
				return;
			}
			mAdapter = new GridItemAdapter(tContext, result);
			stopLoaderAnimation();
			mGridview.setAdapter(mAdapter);
		}

		
	}

}
