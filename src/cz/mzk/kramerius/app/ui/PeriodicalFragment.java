package cz.mzk.kramerius.app.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.Card.OnCardClickListener;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import cz.mzk.kramerius.app.OnItemSelectedListener;
import cz.mzk.kramerius.app.OnOpenDetailListener;
import cz.mzk.kramerius.app.R;
import cz.mzk.kramerius.app.card.OnPopupMenuSelectedListener;
import cz.mzk.kramerius.app.card.PeriodicalCard;
import cz.mzk.kramerius.app.model.Item;
import cz.mzk.kramerius.app.util.CardUtils;
import cz.mzk.kramerius.app.util.ModelUtil;

public class PeriodicalFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		OnOpenDetailListener, OnPopupMenuSelectedListener {

	public static final String TAG = PeriodicalFragment.class.getName();

	private OnItemSelectedListener mCallback;

	private CardGridView mCardGridView;
	private CardGridArrayAdapter mAdapter;

	private DisplayImageOptions mOptions;

	private List<Item> mItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_card_grid, container, false);
		mCardGridView = (CardGridView) view.findViewById(R.id.card_grid);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.search, menu);
		MenuItem item = menu.findItem(R.id.search);
		SearchView searchView = (SearchView) item.getActionView();
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mOptions = CardUtils.initUniversalImageLoaderLibrary(getActivity());
	}

	public void setItems(List<Item> items) {
		if (getActivity() == null) {
			return;
		}
		mItems = items;
		populate(items);
	}

	public void setOnItemSelectedListener(OnItemSelectedListener callback) {
		mCallback = callback;
	}

	// private void onPeriodicalVolumeSelected(int position) {
	// if (mAdapter == null || mAdapter.getCount() < position + 1) {
	// return;
	// }
	// Item item = mAdapter.getItem(position);
	// if (mCallback != null) {
	// mCallback.onItemSelected(item);
	// }
	// }

	private void filterItems(String prefix) {
		if (mAdapter == null || mItems == null) {
			return;
		}
		if(prefix == null) {
			if(mItems.size() > mAdapter.getCount()) {
				populate(mItems);
			}
			return;
		}
		List<Item> items = new ArrayList<Item>();
		for (Item item : mItems) {
			// mAdapter.filter(prefix);
			if (ModelUtil.PERIODICAL_VOLUME.equals(item.getModel())) {
				if ((item.getVolumeNumber() != null && item.getVolumeNumber().startsWith(prefix))
						|| (item.getYear() != null && item.getYear().startsWith(prefix))) {
					items.add(item);
				}
			} else if (ModelUtil.PERIODICAL_ITEM.equals(item.getModel())) {
				if ((item.getDate() != null && item.getDate().startsWith(prefix))
						|| (item.getPartNumber() != null && item.getPartNumber().startsWith(prefix))
						|| (item.getIssueNumber() != null && item.getIssueNumber().startsWith(prefix))) {
					items.add(item);
				}
			}
		}
		populate(items);		
	}
	
	private void populate(List<Item> items) {
		ArrayList<Card> cards = new ArrayList<Card>();
		for (Item item : items) {

			PeriodicalCard card = new PeriodicalCard(getActivity(), item, mOptions);
			card.setOnPopupMenuSelectedListener(this);
			card.setOnClickListener(new OnCardClickListener() {
				@Override
				public void onClick(Card card, View view) {
					if (mCallback != null) {
						mCallback.onItemSelected(((PeriodicalCard) card).getItem());
					}
				}
			});
			cards.add(card);
		}
		mAdapter = new CardGridArrayAdapter(getActivity(), cards);
		CardUtils.setAnimationAdapter(mAdapter, mCardGridView);
	}	

	@Override
	public boolean onQueryTextChange(String query) {
		if (query.length() > 0) {
			filterItems(query);
		} else {
			filterItems(null);
		}
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return onQueryTextChange(query);
	}

	@Override
	public boolean onClose() {
		filterItems(null);
		return false;
	}

	@Override
	public void onOpenDetail(String pid) {
		Intent intent = new Intent(getActivity(), MetadataActivity.class);
		intent.putExtra(MetadataActivity.EXTRA_PID, pid);
		startActivity(intent);
	}

	@Override
	public void onPopupOpenSelectd(Item item) {
		if (mCallback != null) {
			mCallback.onItemSelected(item);
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
