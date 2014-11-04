package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.BaseFragment;
import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.api.K5Connector;
import cz.mzk.kramerius.app.card.GridCard;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.Analytics;
import cz.mzk.kramerius.app.util.CardUtils;

public class SearchResultFragment extends BaseFragment implements OnOpenDetailListener, OnScrollListener,
		OnPopupMenuSelectedListener {

	private static final String EXTRA_QUERY = "extra_query";

	private static final String TAG = SearchResultFragment.class.getSimpleName();

	private String mSearchQuery;
	private OnItemSelectedListener mOnItemSelectedListener;

	private boolean mLoading;
	private int mRows = 30;
	private int mStart = 0;
	private int mNumFound = -1;
	private boolean mFirst = true;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_grid, container, false);
		if (isPhone()) {
			// ScreenUtil.setInsets(getActivity(), view);
		}
		mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
		mCardGridView.setOnScrollListener(this);

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
			if (mFirst) {
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
			if (tContext == null) {
				return;
			}
			if (mFirst) {
				stopLoaderAnimation();
			}
			mLoading = false;
			if (result == null || result.first == null) {
				if (mFirst) {

					showWarningMessage("Nepodařilo se načíst data.", "Opakovat", new onWarningButtonClickedListener() {

						@Override
						public void onWarningButtonClicked() {
							new GetResultTask(getActivity()).execute(mSearchQuery);
						}
					});
				}
				return;
			}
			if (result.second == 0) {
				showWarningMessage("Nebyly nalezeny žádné výsledky.", null, null);
				return;
			}
			mNumFound = result.second;
			populateGrid(result.first);
			mFirst = false;
			int shownNum = mStart + mRows;
			if (shownNum > mNumFound) {
				shownNum = mNumFound;
			}
			
			getSupportActionBar().setSubtitle(shownNum + " z " + mNumFound);
		}
	}

	private void populateGrid(List<Item> items) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Item item : items) {
			GridCard card = new GridCard(getActivity(), item, mOptions);
			card.setOnPopupMenuSelectedListener(this);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					if (mOnItemSelectedListener != null) {
						mOnItemSelectedListener.onItemSelected(((GridCard) card).getItem());
					}
				}
			});
			cards.add(card);
		}

		if (mAdapter == null) {
			mAdapter = CardUtils.createAdapter(getActivity(), items, mOnItemSelectedListener, this, mOptions);
			CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
		} else {
			int index = mCardGridView.getFirstVisiblePosition();
			mAdapter.addAll(cards);
			mCardGridView.setSelection(index);
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

	@Override
	public void onPopupOpenSelectd(Item item) {
		onItemSelected(item);
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
