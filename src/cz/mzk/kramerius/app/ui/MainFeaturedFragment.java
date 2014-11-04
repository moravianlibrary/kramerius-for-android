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
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Api;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;

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

	private CardGridView mCustomGridView;
	private CardGridArrayAdapter mCustomAdapter;
	private List<Item> mCustomList;
	private View mCustomExpandButton;
	// private GridView mNewestGridView;
	// private GridItemAdapter mNewestAdapter;
	private CardGridView mNewestGridView;
	private CardGridArrayAdapter mNewestAdapter;

	private List<Item> mNewestList;
	private View mNewestExpandButton;
	private OnFeaturedListener mCallback;
	private OnItemSelectedListener mOnItemSelectedListener;
	private View mLoaderNewest;
	private View mLoaderCustom;
	
	

	private int mFeaturedLimit;

	private DisplayImageOptions mOptions;

	private TextView mNewestWarning;
	private TextView mCustomWarning;

	private View mCustomAgainButton;
	private View mNewestAgainButton;
	
	private ScrollView mScrollView;

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
			//ScreenUtil.setInsets(getActivity(), view);
		}
		// mLoaderSelected = view.findViewById(R.id.featured_selected_loader);
		mScrollView = (ScrollView) view.findViewById(R.id.featured_scrollview);
		mLoaderNewest = view.findViewById(R.id.featured_newest_loader);
		mLoaderCustom = view.findViewById(R.id.featured_custom_loader);
		// mSelectedGridView = (GridView)
		// view.findViewById(R.id.featured_selected);
		mNewestGridView = (CardGridView) view.findViewById(R.id.featured_newest);
		mCustomGridView = (CardGridView) view.findViewById(R.id.featured_custom);

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
		mCustomExpandButton = view.findViewById(R.id.featured_custom_expand);
		mCustomExpandButton.setOnClickListener(this);
		mNewestAgainButton = view.findViewById(R.id.featured_newest_again);
		mNewestAgainButton.setOnClickListener(this);
		mCustomAgainButton = view.findViewById(R.id.featured_custom_again);
		mCustomAgainButton.setOnClickListener(this);

		mNewestWarning = (TextView) view.findViewById(R.id.featured_newest_warning);
		mCustomWarning = (TextView) view.findViewById(R.id.featured_custom_warning);

		
		String domain = K5Api.getDomain(getActivity());

		
		mCustomExpandButton.setVisibility(View.GONE);
		if (mCustomList == null) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_CUSTOM).execute();
		} else {
			populateGrid(K5Api.FEED_CUSTOM);
		}
		mNewestExpandButton.setVisibility(View.GONE);
		if (mNewestList == null) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_NEWEST).execute();
		} else {
			populateGrid(K5Api.FEED_NEWEST);
		}

//		if (!"krameriusndktest.mzk.cz".equals(domain) && !"kramerius.mzk.cz".equals(domain)) {
//
//			if (mCustomList == null) {
//				new GetFeaturedTask(getActivity(), K5Api.FEED_MOST_DESIRABLE).execute();
//			} else {
//				populateGrid(K5Api.FEED_MOST_DESIRABLE);
//			}
//		}

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
			} else if (tType == K5Api.FEED_CUSTOM) {
				startLoaderAnimation(mLoaderCustom);
				mCustomExpandButton.setVisibility(View.GONE);
				mCustomAgainButton.setVisibility(View.GONE);
				mCustomWarning.setVisibility(View.GONE);				
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {			
			}

		}

		@Override
		protected List<Item> doInBackground(Void... params) {
			return K5Connector.getInstance().getFeatured(tContext, tType, mFeaturedLimit, "public", null);
		}

		@Override
		protected void onPostExecute(List<Item> result) {
			if (tType == K5Api.FEED_CUSTOM) {
				stopLoaderAnimation(mLoaderCustom);
				if (tContext == null || result == null) {
					mCustomAgainButton.setVisibility(View.VISIBLE);
					mCustomWarning.setVisibility(View.VISIBLE);
					return;
				}
				mCustomList = new ArrayList<Item>();
				fillList(result, mCustomList);
			} else if (tType == K5Api.FEED_NEWEST) {
				stopLoaderAnimation(mLoaderNewest);
				if (tContext == null || result == null) {
					mNewestAgainButton.setVisibility(View.VISIBLE);
					mNewestWarning.setVisibility(View.VISIBLE);
					return;
				}
				mNewestList = new ArrayList<Item>();
				fillList(result, mNewestList);
			} else if (tType == K5Api.FEED_MOST_DESIRABLE) {
				
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
		if (type == K5Api.FEED_CUSTOM) {
			mCustomExpandButton.setVisibility(View.VISIBLE);
			mCustomAdapter = CardUtils.createAdapter(getActivity(), mCustomList, mOnItemSelectedListener,
					this, mOptions);
			CardUtils.setAnimationAdapter(mCustomAdapter, mCustomGridView);
			setGridViewHeightBasedOnChildren(mCustomGridView, mFeaturedLimit);
		} else if (type == K5Api.FEED_NEWEST) {
			mNewestExpandButton.setVisibility(View.VISIBLE);
			mNewestAdapter = CardUtils.createAdapter(getActivity(), mNewestList, mOnItemSelectedListener, this,
					mOptions);
			CardUtils.setAnimationAdapter(mNewestAdapter, mNewestGridView);
			setGridViewHeightBasedOnChildren(mNewestGridView, mFeaturedLimit);
		} else if (type == K5Api.FEED_MOST_DESIRABLE) {
			
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
		mScrollView.scrollTo(0,0);
	}

	@Override
	public void onClick(View v) {
		if (mCallback == null) {
			return;
		}
		if (v == mNewestExpandButton) {
			mCallback.onFeatured(K5Api.FEED_NEWEST);
		} else if (v == mCustomExpandButton) {
			mCallback.onFeatured(K5Api.FEED_CUSTOM);
		} else if (v == mNewestAgainButton) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_NEWEST).execute();
		} else if (v == mCustomAgainButton) {
			new GetFeaturedTask(getActivity(), K5Api.FEED_CUSTOM).execute();
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
		mScrollView.scrollTo(0,0);
	}

}
