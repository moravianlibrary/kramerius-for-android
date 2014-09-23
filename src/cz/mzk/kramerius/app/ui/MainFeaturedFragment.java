package cz.mzk.kramerius.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;
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

public class MainFeaturedFragment extends BaseFragment implements OnClickListener, OnOpenDetailListener {

	private static final int FEATURED_PHONE_LIMIT = 4;
	private static final int FEATURED_TABLET_LIMIT = 5;

	private static final String TAG = MainFeaturedFragment.class.getSimpleName();

	private GridView mSelectedGridView;
	private GridItemAdapter mSelectedAdapter;
	private List<Item> mSelectedList;
	private View mSelectedExpandButton;
	private GridView mMostDesirableGridView;
	private GridItemAdapter mMostDesirableAdapter;
	private List<Item> mMostDesirableList;
	private View mMostDesirableExpandButton;
	private GridView mNewestGridView;
	private GridItemAdapter mNewestAdapter;
	private List<Item> mNewestList;
	private View mNewestExpandButton;
	private OnFeaturedListener mCallback;
	private OnItemSelectedListener mOnItemSelectedListener;
	private View mLoaderSelected;
	private View mLoaderNewest;
	private View mLoaderMostDesirable;

	private int mFeaturedLimit;

	public interface OnFeaturedListener {
		public void onFeatured(int type);
	}

	public void setCallback(OnFeaturedListener callback) {
		mCallback = callback;
	}

	public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
		mOnItemSelectedListener = onItemSelectedListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFeaturedLimit = isTablet() ? FEATURED_TABLET_LIMIT : FEATURED_PHONE_LIMIT;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_featured_main, container, false);
		if (isPhone()) {
			ScreenUtil.setInsets(getActivity(), view);
		}
		// TextView tt = (TextView) view.findViewById(R.id.test);
		// tt.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),
		// "Roboto-Thin.ttf"));
		mLoaderSelected = view.findViewById(R.id.featured_selected_loader);
		mLoaderNewest = view.findViewById(R.id.featured_newest_loader);
		mLoaderMostDesirable = view.findViewById(R.id.featured_mostdesirable_loader);
		mSelectedGridView = (GridView) view.findViewById(R.id.featured_selected);
		mNewestGridView = (GridView) view.findViewById(R.id.featured_newest);
		mMostDesirableGridView = (GridView) view.findViewById(R.id.featured_mostdesirable);

		mSelectedGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (mSelectedAdapter == null) {
					return;
				}
				final Item item = mSelectedAdapter.getGridItem(position);
				onItemSelected(item);
			}
		});

		mNewestGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (mNewestAdapter == null) {
					return;
				}
				final Item item = mNewestAdapter.getGridItem(position);
				onItemSelected(item);
			}
		});

		mMostDesirableGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (mMostDesirableAdapter == null) {
					return;
				}
				final Item item = mMostDesirableAdapter.getGridItem(position);
				onItemSelected(item);
			}
		});

		mSelectedExpandButton = view.findViewById(R.id.featured_selected_expand);
		mSelectedExpandButton.setOnClickListener(this);
		mNewestExpandButton = view.findViewById(R.id.featured_newest_expand);
		mNewestExpandButton.setOnClickListener(this);
		mMostDesirableExpandButton = view.findViewById(R.id.featured_mostdesirable_expand);
		mMostDesirableExpandButton.setOnClickListener(this);

		// if (mSelectedList == null) {
		// mSelectedExpandButton.setVisibility(View.GONE);
		// startLoaderAnimation(mLoaderSelected);
		// new GetFeaturedTask(getActivity(), K5Api.FEED_SELECTED).execute();
		// } else {
		// populateGrid(K5Api.FEED_SELECTED);
		// }

		if (mMostDesirableList == null) {
			mMostDesirableExpandButton.setVisibility(View.GONE);
			startLoaderAnimation(mLoaderMostDesirable);
			new GetFeaturedTask(getActivity(), K5Api.FEED_MOST_DESIRABLE).execute();
		} else {
			populateGrid(K5Api.FEED_MOST_DESIRABLE);
		}

		if (mNewestList == null) {
			mNewestExpandButton.setVisibility(View.GONE);
			startLoaderAnimation(mLoaderNewest);
			new GetFeaturedTask(getActivity(), K5Api.FEED_NEWEST).execute();
		} else {
			populateGrid(K5Api.FEED_NEWEST);
		}

		return view;
	}

	private void onItemSelected(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	class GetFeaturedTask extends AsyncTask<Void, Void, List<Item>> {

		private Context tContext;
		private int tType;

		public GetFeaturedTask(Context context, int type) {
			tContext = context;
			tType = type;

		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected List<Item> doInBackground(Void... params) {
			if (tType == K5Api.FEED_NEWEST) {
				return K5Connector.getInstance().getNewest(tContext, true, mFeaturedLimit);
			} else if (tType == K5Api.FEED_SELECTED) {
				return K5Connector.getInstance().getSelected(tContext, true, mFeaturedLimit);
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				return K5Connector.getInstance().getMostDesirable(tContext, true, mFeaturedLimit);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if (tContext == null || result == null) {
				return;
			}
			if (tType == K5Api.FEED_SELECTED) {
				stopLoaderAnimation(mLoaderSelected);
				mSelectedExpandButton.setVisibility(View.VISIBLE);
				mSelectedList = new ArrayList<Item>();
				fillList(result, mSelectedList);
			} else if (tType == K5Api.FEED_NEWEST) {
				stopLoaderAnimation(mLoaderNewest);
				mNewestExpandButton.setVisibility(View.VISIBLE);
				mNewestList = new ArrayList<Item>();
				fillList(result, mNewestList);
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				stopLoaderAnimation(mLoaderMostDesirable);
				mMostDesirableExpandButton.setVisibility(View.VISIBLE);
				mMostDesirableList = new ArrayList<Item>();
				fillList(result, mMostDesirableList);
			}
			populateGrid(tType);
		}
	}

	private void fillList(List<Item> src, List<Item> dst) {
		if (src.size() > mFeaturedLimit) {
			dst.addAll(src.subList(0, mFeaturedLimit));
		} else {
			dst.addAll(src);
		}
	}

	private void populateGrid(int type) {
		if (getActivity() == null) {
			return;
		}
		if (type == K5Api.FEED_SELECTED) {
			mSelectedAdapter = new GridItemAdapter(getActivity(), mSelectedList, this);
			mSelectedGridView.setAdapter(mSelectedAdapter);
			setGridViewHeightBasedOnChildren(mSelectedGridView, mFeaturedLimit);
		} else if (type == K5Api.FEED_NEWEST) {
			mNewestAdapter = new GridItemAdapter(getActivity(), mNewestList, this);
			mNewestGridView.setAdapter(mNewestAdapter);
			setGridViewHeightBasedOnChildren(mNewestGridView, mFeaturedLimit);
		} else if (type == K5Api.FEED_MOST_DESIRABLE) {
			mMostDesirableAdapter = new GridItemAdapter(getActivity(), mMostDesirableList, this);
			mMostDesirableGridView.setAdapter(mMostDesirableAdapter);
			setGridViewHeightBasedOnChildren(mMostDesirableGridView, mFeaturedLimit);
		}
	}

	private void setGridViewHeightBasedOnChildren(GridView gridView, int count) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int items = listAdapter.getCount();
		if (listAdapter.getCount() > 0) {
			View listItem = listAdapter.getView(0, null, gridView);
			listItem.measure(0, 0);
			totalHeight = listItem.getMeasuredHeight();
			if (isPhone()) {
				int c = Math.min(items, count);
				totalHeight = totalHeight * c;
			} else {
				totalHeight += getResources().getDisplayMetrics().density * 10;
			}
		}

		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight;
		gridView.setLayoutParams(params);

	}

	@Override
	public void onClick(View v) {
		if (mCallback == null) {
			return;
		}
		if (v == mSelectedExpandButton) {
			mCallback.onFeatured(K5Api.FEED_SELECTED);
		} else if (v == mNewestExpandButton) {
			mCallback.onFeatured(K5Api.FEED_NEWEST);
		} else if (v == mMostDesirableExpandButton) {
			mCallback.onFeatured(K5Api.FEED_MOST_DESIRABLE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.sendScreenView(getActivity(), R.string.ga_appview_main_featured);
	}

	@Override
	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

}
