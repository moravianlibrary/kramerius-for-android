package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.BaseFragment.onWarningButtonClickedListener;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.ui.PageActivity.LoadPagesTask;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.MessageUtils;
import cz.mzk.kramerius.app.util.ScreenUtil;

public class MainFeaturedFragment extends BaseFragment implements OnClickListener, OnOpenDetailListener,
		OnPopupMenuSelectedListener {

	private static final int FEATURED_PHONE_LIMIT = 4;
	private static final int FEATURED_TABLET_LIMIT = 5;

	private static final String TAG = MainFeaturedFragment.class.getSimpleName();

	// private GridView mSelectedGridView;
	// private GridItemAdapter mSelectedAdapter;
	// private List<Item> mSelectedList;
	// private View mSelectedExpandButton;
	// private View mLoaderSelected;

	private CardGridView mMostDesirableGridView;
	private CardGridArrayAdapter mMostDesirableAdapter;
	private List<Item> mMostDesirableList;
	private View mMostDesirableExpandButton;
	// private GridView mNewestGridView;
	// private GridItemAdapter mNewestAdapter;
	private CardGridView mNewestGridView;
	private CardGridArrayAdapter mNewestAdapter;

	private List<Item> mNewestList;
	private View mNewestExpandButton;
	private OnFeaturedListener mCallback;
	private OnItemSelectedListener mOnItemSelectedListener;
	private View mLoaderNewest;
	private View mLoaderMostDesirable;

	private int mFeaturedLimit;

	private DisplayImageOptions mOptions;

	private TextView mNewestWarning;
	private TextView mMostDesirableWarning;

	private View mMostDesirableAgainButton;
	private View mNewestAgainButton;

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
		// mLoaderSelected = view.findViewById(R.id.featured_selected_loader);
		mLoaderNewest = view.findViewById(R.id.featured_newest_loader);
		mLoaderMostDesirable = view.findViewById(R.id.featured_mostdesirable_loader);
		// mSelectedGridView = (GridView)
		// view.findViewById(R.id.featured_selected);
		mNewestGridView = (CardGridView) view.findViewById(R.id.featured_newest);
		mMostDesirableGridView = (CardGridView) view.findViewById(R.id.featured_mostdesirable);

		// mSelectedGridView.setOnItemClickListener(new
		// AdapterView.OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> a, View v, int position, long
		// id) {
		// if (mSelectedAdapter == null) {
		// return;
		// }
		// final Item item = mSelectedAdapter.getGridItem(position);
		// onItemSelected(item);
		// }
		// });

		// mSelectedExpandButton =
		// view.findViewById(R.id.featured_selected_expand);
		// mSelectedExpandButton.setOnClickListener(this);
		mNewestExpandButton = view.findViewById(R.id.featured_newest_expand);
		mNewestExpandButton.setOnClickListener(this);
		mMostDesirableExpandButton = view.findViewById(R.id.featured_mostdesirable_expand);
		mMostDesirableExpandButton.setOnClickListener(this);
		mNewestAgainButton = view.findViewById(R.id.featured_newest_again);
		mNewestAgainButton.setOnClickListener(this);
		mMostDesirableAgainButton = view.findViewById(R.id.featured_mostdesirable_again);
		mMostDesirableAgainButton.setOnClickListener(this);

		mNewestWarning = (TextView) view.findViewById(R.id.featured_newest_warning);
		mMostDesirableWarning = (TextView) view.findViewById(R.id.featured_mostdesirable_warning);

		// if (mSelectedList == null) {
		// mSelectedExpandButton.setVisibility(View.GONE);
		// startLoaderAnimation(mLoaderSelected);
		// new GetFeaturedTask(getActivity(), K5Api.FEED_SELECTED).execute();
		// } else {
		// populateGrid(K5Api.FEED_SELECTED);
		// }

		String domain = K5Api.getDomain(getActivity());

		mNewestExpandButton.setVisibility(View.GONE);
		mMostDesirableExpandButton.setVisibility(View.GONE);
		if (mNewestList == null) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_NEWEST).execute();
		} else {
			populateGrid(K5Api.FEED_NEWEST);
		}

		if (!"krameriusndktest.mzk.cz".equals(domain) && !"kramerius.mzk.cz".equals(domain)) {

			if (mMostDesirableList == null) {
				new GetFeaturedTask(getActivity(), K5Api.FEED_MOST_DESIRABLE).execute();
			} else {
				populateGrid(K5Api.FEED_MOST_DESIRABLE);
			}
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void onItemSelected(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
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
			if (tType == K5Api.FEED_NEWEST) {
				startLoaderAnimation(mLoaderNewest);
				mNewestExpandButton.setVisibility(View.GONE);
				mNewestAgainButton.setVisibility(View.GONE);
				mNewestWarning.setVisibility(View.GONE);
			} else if (tType == K5Api.FEED_SELECTED) {
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				startLoaderAnimation(mLoaderMostDesirable);
				mMostDesirableExpandButton.setVisibility(View.GONE);
				mMostDesirableAgainButton.setVisibility(View.GONE);
				mMostDesirableWarning.setVisibility(View.GONE);

			}

		}

		@Override
		protected List<Item> doInBackground(Void... params) {
			if (tType == K5Api.FEED_NEWEST) {
				return K5Connector.getInstance().getNewest(tContext, mFeaturedLimit);
			} else if (tType == K5Api.FEED_SELECTED) {
				return K5Connector.getInstance().getSelected(tContext, true, mFeaturedLimit);
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				return K5Connector.getInstance().getMostDesirable(tContext, mFeaturedLimit);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if (tType == K5Api.FEED_SELECTED) {
				// stopLoaderAnimation(mLoaderSelected);
				// mSelectedExpandButton.setVisibility(View.VISIBLE);
				// mSelectedList = new ArrayList<Item>();
				// fillList(result, mSelectedList);
			} else if (tType == K5Api.FEED_NEWEST) {
				stopLoaderAnimation(mLoaderNewest);
				if (tContext == null || result == null) {
					mNewestAgainButton.setVisibility(View.VISIBLE);
					mNewestWarning.setVisibility(View.VISIBLE);
					return;
				}
				mNewestExpandButton.setVisibility(View.VISIBLE);
				mNewestList = new ArrayList<Item>();
				fillList(result, mNewestList);
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				stopLoaderAnimation(mLoaderMostDesirable);
				if (tContext == null || result == null) {
					mMostDesirableAgainButton.setVisibility(View.VISIBLE);
					mMostDesirableWarning.setVisibility(View.VISIBLE);
					return;
				}
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
			// mSelectedAdapter = new GridItemAdapter(getActivity(),
			// mSelectedList, this);
			// mSelectedGridView.setAdapter(mSelectedAdapter);
			// setGridViewHeightBasedOnChildren(mSelectedGridView,
			// mFeaturedLimit);
		} else if (type == K5Api.FEED_NEWEST) {
			mNewestAdapter = CardUtils.createAdapter(getActivity(), mNewestList, mOnItemSelectedListener, this,
					mOptions);
			CardUtils.setAnimationAdapter(mNewestAdapter, mNewestGridView);
			setGridViewHeightBasedOnChildren(mNewestGridView, mFeaturedLimit);
		} else if (type == K5Api.FEED_MOST_DESIRABLE) {
			mMostDesirableAdapter = CardUtils.createAdapter(getActivity(), mMostDesirableList, mOnItemSelectedListener,
					this, mOptions);
			CardUtils.setAnimationAdapter(mMostDesirableAdapter, mMostDesirableGridView);
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
				totalHeight = totalHeight * c + (int) (getResources().getDisplayMetrics().density * 10);
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
		if (v == mNewestExpandButton) {
			mCallback.onFeatured(K5Api.FEED_NEWEST);
		} else if (v == mMostDesirableExpandButton) {
			mCallback.onFeatured(K5Api.FEED_MOST_DESIRABLE);
		} else if (v == mNewestAgainButton) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_NEWEST).execute();
		} else if (v == mMostDesirableAgainButton) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_MOST_DESIRABLE).execute();
		}

		// else if (v == mSelectedExpandButton) {
		// mCallback.onFeatured(K5Api.FEED_SELECTED);
		// }
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

	@Override
	public void onPopupOpenSelectd(Item item) {
		if (mOnItemSelectedListener != null) {
			mOnItemSelectedListener.onItemSelected(item);
		}
	}

	@Override
	public void onPopupDetailsSelectd(Item item) {
		onOpenDetail(item.getPid());
	}

	@Override
	public void onPopupShareSelectd(Item item) {
		// TODO Auto-generated method stub

	}

}
